package co.edu.unbosque.conclave.service;

import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.edu.unbosque.conclave.dto.AIResponseDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;
import co.edu.unbosque.conclave.exception.ApiKeyNotConfiguredException;
import co.edu.unbosque.conclave.exception.UnsupportedOutputTypeException;
import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.MediaFormat;
import co.edu.unbosque.conclave.model.ResponseStatus;

/**
 * Coordina la generación de imágenes, audio y video mediante proveedores externos.
 * Los videos se generan con modelos alojados en Spaces de Hugging Face.
 */
@Service
public class PromptService {
    private static final Logger LOGGER = Logger.getLogger(PromptService.class.getName());
    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String CLOUDFLARE_BASE = "https://api.cloudflare.com/client/v4/accounts/";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    /**
     * Configuración de un Space de Hugging Face usado para generar video a partir de texto.
     *
     * @param space identificador del Space con el formato {@code organizacion/nombre}
     * @param apiName nombre del endpoint de Gradio, sin la barra inicial
     * @param data función que construye los parámetros del endpoint a partir del prompt
     */
    private record HfSpace(String space, String apiName, Function<String, JsonArray> data) {}

    /**
     * Modelos de texto a video de Hugging Face disponibles. Cada proveedor usa su propia API key.
     */
    private static final Map<AIProvider, HfSpace> HF_SPACES = Map.of(
            AIProvider.HF_MINIMAX_H3, new HfSpace(
                    "hugging-apps/minimax-h3-flashgen-4step",
                    "generate",
                    prompt -> {
                        JsonArray data = new JsonArray();
                        data.add(prompt);
                        data.add(JsonNull.INSTANCE);
                        data.add(JsonNull.INSTANCE);
                        data.add("960x544 \u00B7 16:9 fast");
                        data.add(5);
                        data.add(42);
                        data.add(false);
                        return data;
                    }),
            AIProvider.HF_MINIMAX_TURBO, new HfSpace(
                    "wank3r/minimax-h3",
                    "generate",
                    prompt -> {
                        JsonArray data = new JsonArray();
                        data.add(prompt);
                        data.add(JsonNull.INSTANCE);
                        data.add(JsonNull.INSTANCE);
                        data.add("960x544 \u00B7 16:9 fast");
                        data.add(5);
                        data.add(7);
                        data.add(-1);
                        data.add(false);
                        data.add(true);
                        data.add(new JsonArray());
                        data.add("");
                        data.add(1.0);
                        data.add("");
                        return data;
                    }),
            AIProvider.HF_OMNI_VIDEO, new HfSpace(
                    "CumaSystem/Omni-videos-custom",
                    "_submit_t2v",
                    prompt -> {
                        JsonArray data = new JsonArray();
                        data.add("1");
                        data.add("3");
                        data.add("384");
                        data.add("16:9");
                        data.add(prompt);
                        data.add(prompt);
                        data.add("");
                        data.add("");
                        data.add("");
                        return data;
                    }),
            AIProvider.HF_MODELSCOPE, new HfSpace(
                    "mediasynthesismuseum/modelscope-text-to-video",
                    "generate_1",
                    prompt -> {
                        JsonArray data = new JsonArray();
                        data.add(prompt);
                        data.add(-1);
                        data.add(24);
                        data.add(25);
                        return data;
                    })
    );

    @Autowired private ExternalHTTPRequestHandler externalHandler;
    @Autowired private ApiKeyService apiKeyService;

    /**
     * Genera el contenido solicitado y devuelve su respuesta.
     *
     * @param request parámetros de generación
     * @return lista con el resultado del proveedor seleccionado
     * @throws ApiKeyNotConfiguredException si falta la clave de un proveedor
     * @throws UnsupportedOutputTypeException si el tipo de salida no está soportado
     */
    public List<AIResponseDTO> generar(PromptRequestDTO request) throws ApiKeyNotConfiguredException, UnsupportedOutputTypeException {
        if (request == null || request.getOutputType() == null) throw new IllegalArgumentException("outputType es obligatorio");
        AIResponseDTO result = switch (request.getOutputType()) {
            case IMAGE -> generateImage(request);
            case AUDIO -> generateAudio(request);
            case VIDEO -> generateVideo(request);
            default -> throw new UnsupportedOutputTypeException(AIProvider.GEMINI, request.getOutputType());
        };
        return List.of(result);
    }

    /**
     * Selecciona el proveedor de imagenes y valida su identificador.
     *
     * <p>Proveedores soportados:</p>
     * <ul>
     *   <li>{@code CLOUDFLARE} — Cloudflare Workers AI, FLUX.1 schnell (valor por defecto).</li>
     *   <li>{@code POLLINATIONS} — Pollinations.ai, gratuito y con API key opcional.</li>
     *   <li>{@code TOGETHER_AI} — Together AI, FLUX.1 schnell gratuito.</li>
     *   <li>{@code GEMINI} — Google Gemini imagen.</li>
     *   <li>{@code OPENROUTER} — 4 presets en paralelo via OpenRouter.</li>
     * </ul>
     *
     * @param request parametros de generacion
     * @return respuesta de generacion de imagen
     */
    private AIResponseDTO generateImage(PromptRequestDTO request) {
        String provider = request.getImageProvider() == null ? "" : request.getImageProvider().trim().toUpperCase();
        return switch (provider) {
            case "", "CLOUDFLARE" -> generateCloudflareImage(request);
            case "POLLINATIONS" -> generatePollinationsImage(request);
            case "TOGETHER_AI" -> generateTogetherImage(request);
            case "GEMINI" -> generateGeminiImage(request);
            case "OPENROUTER" -> generateOpenRouterImages(request);
            default -> failed(base(AIProvider.CLOUDFLARE, "image-provider", 400,
                    System.currentTimeMillis()), "Proveedor de imagen no valido: " + provider);
        };
    }

    /**
     * Genera una imagen con Pollinations.ai. Funciona sin API key en el nivel anónimo;
     * si hay una key configurada se envía en la cabecera para quitar la marca de agua
     * y el límite de velocidad. La imagen se descarga en memoria y se entrega al
     * usuario sin guardarse en la base de datos.
     *
     * @param request parámetros de generación
     * @return respuesta con la imagen generada o el detalle del error
     */
    private AIResponseDTO generatePollinationsImage(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String model = "pollinations-flux";
        try {
            String prompt = URLEncoder.encode(request.getPromptText(), StandardCharsets.UTF_8).replace("+", "%20");
            long seed = (long) (Math.random() * 1_000_000);
            String url = "https://image.pollinations.ai/prompt/" + prompt
                    + "?width=1024&height=1024&model=flux&nologo=true&seed=" + seed;
            String token = optionalKey(AIProvider.POLLINATIONS);
            Map<String, String> headers = token == null ? Map.of() : Map.of(AUTHORIZATION, BEARER + token);
            HttpResponse<byte[]> response = externalHandler.doGetBytesAsync(url, headers).join();
            AIResponseDTO dto = base(AIProvider.POLLINATIONS, model, response.statusCode(), start);
            String mime = response.headers().firstValue(CONTENT_TYPE).orElse("image/jpeg");
            if (!ok(response.statusCode()) || !mime.startsWith("image/")) {
                return failed(dto, "Pollinations: " + bytesError(response));
            }
            return successImage(dto, mime, Base64.getEncoder().encodeToString(response.body()), start);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Pollinations image generation failed", e);
            return failed(base(AIProvider.POLLINATIONS, model, 500, start), "Error Pollinations: " + e.getMessage());
        }
    }

    /**
     * Genera una imagen con el modelo gratuito FLUX.1 schnell de Together AI.
     *
     * @param request parámetros de generación
     * @return respuesta con la imagen generada o el detalle del error
     */
    private AIResponseDTO generateTogetherImage(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String model = "black-forest-labs/FLUX.1-schnell-Free";
        String apiKey = optionalKey(AIProvider.TOGETHER_AI);
        if (apiKey == null) {
            return failed(base(AIProvider.TOGETHER_AI, model, 400, start),
                    "Configura la API key de Together AI en el panel de administración.");
        }
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", model);
            body.addProperty("prompt", request.getPromptText());
            body.addProperty("width", 1024);
            body.addProperty("height", 768);
            body.addProperty("steps", 4);
            body.addProperty("n", 1);
            body.addProperty("response_format", "b64_json");
            HttpResponse<String> response = post("https://api.together.xyz/v1/images/generations",
                    Map.of(CONTENT_TYPE, APPLICATION_JSON, AUTHORIZATION, BEARER + apiKey), body.toString());
            AIResponseDTO dto = base(AIProvider.TOGETHER_AI, model, response.statusCode(), start);
            if (!ok(response)) return failed(dto, "Together AI: " + error(response));
            JsonArray data = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("data");
            if (data == null || data.isEmpty()) return failed(dto, "Together AI no devolvió imágenes.");
            JsonObject first = data.get(0).getAsJsonObject();
            String b64 = stringValue(first, "b64_json");
            if (b64 != null) return successImage(dto, mimeFromBase64(b64), b64, start);
            String url = stringValue(first, "url");
            if (url == null) return failed(dto, "Together AI no devolvió datos de imagen.");
            dto.setMimeType("image/jpeg");
            dto.setDownloadUrl(url);
            dto.setFormat(MediaFormat.JPEG);
            dto.setStatus(ResponseStatus.SUCCESS);
            dto.setDurationMs(System.currentTimeMillis() - start);
            return dto;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Together AI image generation failed", e);
            return failed(base(AIProvider.TOGETHER_AI, model, 500, start), "Error Together AI: " + e.getMessage());
        }
    }

    /**
     * Marca la respuesta como exitosa con una imagen codificada en base64.
     *
     * @param dto respuesta que se actualizará
     * @param mime tipo MIME de la imagen
     * @param base64 contenido de la imagen en base64
     * @param start instante en que inició la generación
     * @return respuesta exitosa con la imagen como data URL
     */
    private AIResponseDTO successImage(AIResponseDTO dto, String mime, String base64, long start) {
        dto.setMimeType(mime);
        dto.setDownloadUrl("data:" + mime + ";base64," + base64);
        dto.setFormat(mime.contains("png") ? MediaFormat.PNG : mime.contains("webp") ? MediaFormat.WEBP : MediaFormat.JPEG);
        dto.setStatus(ResponseStatus.SUCCESS);
        dto.setDurationMs(System.currentTimeMillis() - start);
        return dto;
    }

    /**
     * Detecta el tipo MIME de una imagen a partir del inicio de su contenido en base64.
     *
     * @param base64 contenido de la imagen en base64
     * @return tipo MIME detectado; {@code image/jpeg} si no se reconoce
     */
    private String mimeFromBase64(String base64) {
        if (base64.startsWith("iVBOR")) return "image/png";
        if (base64.startsWith("UklGR")) return "image/webp";
        return "image/jpeg";
    }

    /**
     * Genera imagenes lanzando en paralelo los 4 presets de OpenRouter.
     *
     * <p>Cada preset corre en un {@link CompletableFuture} independiente.
     * Si un preset falla, su {@link AIResponseDTO} lleva
     * {@link ResponseStatus#FAILED} sin afectar los demas.</p>
     *
     * <p>Los presets usados son:</p>
     * <ul>
     *   <li>{@code @preset/conclave-imagen-gpt}</li>
     *   <li>{@code @preset/conclave-imagen-gemini}</li>
     *   <li>{@code @preset/conclave-imagen-flux}</li>
     *   <li>{@code @preset/conclave-imagen-seedream}</li>
     * </ul>
     *
     * @param request parametros de generacion
     * @return primer resultado exitoso, o el primero fallido si todos fallan
     */
    private AIResponseDTO generateOpenRouterImages(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String apiKey;
        try {
            apiKey = key(AIProvider.OPENROUTER);
        } catch (Exception e) {
            return failed(base(AIProvider.OPENROUTER, "openrouter-image", 400, start),
                    "Configura la API Key de OpenRouter en Swagger: /admin/apikey/update/OPENROUTER.");
        }

        List<String> presets = List.of(
                "@preset/conclave-imagen-gpt",
                "@preset/conclave-imagen-gemini",
                "@preset/conclave-imagen-flux",
                "@preset/conclave-imagen-seedream"
        );

        Map<String, String> headers = Map.of(
                "Authorization", "Bearer " + apiKey,
                "Content-Type", "application/json",
                "HTTP-Referer", "https://conclave.unbosque.edu.co",
                "X-Title", "Conclave"
        );

        List<CompletableFuture<AIResponseDTO>> futuros = new ArrayList<>();
        for (String preset : presets) {
            String finalApiKey = apiKey;
            CompletableFuture<AIResponseDTO> futuro = externalHandler
                    .doPostAsync(openRouterUrl(), headers, construirBodyOpenRouter(preset, request))
                    .thenApply(response -> parsearRespuestaOpenRouter(response, preset, start))
                    .exceptionally(ex -> {
                        LOGGER.log(Level.WARNING,
                                "Error en preset OpenRouter {0}: {1}",
                                new Object[]{preset, ex.getMessage()});
                        return failed(base(AIProvider.OPENROUTER, preset, 500, start),
                                ex.getMessage());
                    });
            futuros.add(futuro);
        }

        return futuros.stream()
                .map(CompletableFuture::join)
                .filter(dto -> ResponseStatus.SUCCESS.equals(dto.getStatus()))
                .findFirst()
                .orElseGet(() -> futuros.get(0).join());
    }

    /**
     * Construye el JSON del body para una peticion a OpenRouter.
     *
     * @param presetId ID del preset de OpenRouter
     * @param request  request del usuario
     * @return cadena JSON lista para enviar
     */
    private String construirBodyOpenRouter(String presetId, PromptRequestDTO request) {
        String contenido = request.getPromptText();
        if (request.getFormat() != null) {
            contenido += " Formato de salida: " + request.getFormat().getExtension();
        }

        JsonObject mensaje = new JsonObject();
        mensaje.addProperty("role", "user");
        mensaje.addProperty("content", contenido);

        JsonArray mensajes = new JsonArray();
        mensajes.add(mensaje);

        JsonObject body = new JsonObject();
        body.addProperty("model", presetId);
        body.add("messages", mensajes);

        return body.toString();
    }

    /**
     * Parsea la respuesta de OpenRouter y la convierte en un {@link AIResponseDTO}.
     *
     * <p>Lee {@code choices[0].message.content} como resultado. Si la respuesta
     * no es 2xx o el campo no existe, retorna un DTO con
     * {@link ResponseStatus#FAILED}.</p>
     *
     * @param response respuesta HTTP de OpenRouter
     * @param presetId ID del preset que genero la respuesta
     * @param inicio   marca de tiempo antes de lanzar la peticion
     * @return DTO con la respuesta normalizada
     */
    private AIResponseDTO parsearRespuestaOpenRouter(
            HttpResponse<String> response, String presetId, long inicio) {

        AIResponseDTO dto = base(AIProvider.OPENROUTER, presetId, response.statusCode(), inicio);

        if (!ok(response)) {
            return failed(dto, "OpenRouter respondio con HTTP "
                    + response.statusCode() + " para " + presetId);
        }

        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices");

            if (choices == null || choices.isEmpty()) {
                return failed(dto, "OpenRouter no devolvio choices para " + presetId);
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            String content = message.has("content") && !message.get("content").isJsonNull()
                    ? message.get("content").getAsString()
                    : "";

            dto.setDownloadUrl(content);
            dto.setFormat(response.request() != null ? MediaFormat.PNG : MediaFormat.PNG);
            dto.setMimeType("image/png");
            dto.setStatus(ResponseStatus.SUCCESS);
            return dto;

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,
                    "Error al parsear respuesta OpenRouter {0}: {1}",
                    new Object[]{presetId, ex.getMessage()});
            return failed(dto, "Error al parsear respuesta de " + presetId + ": " + ex.getMessage());
        }
    }

    /**
     * Retorna la URL del endpoint de OpenRouter desde application.properties.
     *
     * @return URL del endpoint de chat completions de OpenRouter
     */
    private String openRouterUrl() {
        return "https://openrouter.ai/api/v1/chat/completions";
    }

    /**
     * Genera una imagen mediante Cloudflare Workers AI.
     *
     * @param request parámetros de generación
     * @return respuesta con la imagen generada o el detalle del error
     */
    /** Genera una imagen mediante Cloudflare Workers AI.
     * @param request parámetros de generación
     * @return resultado de la generación
     */
    /**
     * Genera una imagen mediante Cloudflare Workers AI.
     *
     * @param request parámetros de generación
     * @return respuesta con la imagen o el detalle del error
     */
    private AIResponseDTO generateCloudflareImage(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String credentials = key(AIProvider.CLOUDFLARE);
        String[] parts = credentials.split("\\|", 2);
        if (!validCloudflareCredentials(parts)) {
            return failed(base(AIProvider.CLOUDFLARE, "@cf/black-forest-labs/flux-1-schnell", 400, start),
                    "Cloudflare key inválida. Configura ACCOUNT_ID|API_TOKEN en Swagger.");
        }
        String accountId = parts[0].trim();
        String token = parts[1].trim();
        String model = "@cf/black-forest-labs/flux-1-schnell";
        JsonObject body = new JsonObject();
        body.addProperty("prompt", request.getPromptText());
        body.addProperty("steps", 4);
        String url = "https://api.cloudflare.com/client/v4/accounts/" + accountId + "/ai/run/" + model;
        HttpResponse<String> response = post(url, Map.of("Content-Type", "application/json", "Authorization", "Bearer " + token), body.toString());
        AIResponseDTO dto = base(AIProvider.CLOUDFLARE, model, response.statusCode(), start);
        if (!ok(response)) return failed(dto, error(response));
        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String imageData = cloudflareImageData(json);
            if (imageData == null || imageData.isBlank()) return failed(dto, "Cloudflare no devolvió datos de imagen.");
            if (imageData.startsWith("data:")) {
                dto.setMimeType(imageData.substring(5, imageData.indexOf(';')));
                dto.setDownloadUrl(imageData);
            } else {
                dto.setMimeType("image/png");
                dto.setDownloadUrl("data:image/png;base64," + imageData);
            }
            dto.setFormat(request.getFormat() == null ? MediaFormat.PNG : request.getFormat());
            dto.setStatus(ResponseStatus.SUCCESS);
            return dto;
        } catch (Exception e) { return failed(dto, "No se pudo interpretar la respuesta de imagen de Cloudflare: " + e.getMessage()); }
    }

    /** Valida que Cloudflare haya recibido el identificador y el token requeridos. */
    private boolean validCloudflareCredentials(String[] parts) {
        return parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank();
    }

    /** Extrae los datos de imagen de los formatos de respuesta admitidos por Cloudflare. */
    private String cloudflareImageData(JsonObject json) {
        if (json.has("image") && !json.get("image").isJsonNull()) {
            return json.get("image").getAsString();
        }
        if (json.has("result") && json.get("result").isJsonObject()) {
            JsonObject result = json.getAsJsonObject("result");
            if (result.has("image") && !result.get("image").isJsonNull()) {
                return result.get("image").getAsString();
            }
        }
        return null;
    }

    /**
     * Genera una imagen utilizando el modelo de imágenes de Gemini.
     *
     * @param request parámetros de generación
     * @return respuesta con la imagen generada o el detalle del error
     */
    private AIResponseDTO generateGeminiImage(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String apiKey;
        try { apiKey = key(AIProvider.GEMINI); }
        catch (Exception e) { return failed(base(AIProvider.GEMINI, "gemini-2.5-flash-image", 400, start), "Configura la API Key de Gemini en Swagger."); }
        String model = "gemini-2.5-flash-image";
        JsonObject body = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject prompt = new JsonObject();
        prompt.addProperty("text", request.getPromptText());
        parts.add(prompt);

        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        body.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        JsonArray modalities = new JsonArray();
        modalities.add("TEXT");
        modalities.add("IMAGE");
        generationConfig.add("responseModalities", modalities);
        body.add("generationConfig", generationConfig);
        HttpResponse<String> response = post(GEMINI_BASE + model + ":generateContent", Map.of("Content-Type", "application/json", "x-goog-api-key", apiKey), body.toString());
        AIResponseDTO dto = base(AIProvider.GEMINI, model, response.statusCode(), start);
        if (!ok(response)) return failed(dto, error(response));
        try {
            JsonArray outParts = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts");
            for (var part : outParts) if (part.isJsonObject() && part.getAsJsonObject().has("inlineData")) {
                JsonObject data = part.getAsJsonObject().getAsJsonObject("inlineData");
                String mime = data.has("mimeType") ? data.get("mimeType").getAsString() : "image/png";
                String base64 = data.get("data").getAsString();
                dto.setMimeType(mime);
                dto.setDownloadUrl("data:" + mime + ";base64," + base64);
                dto.setFormat(request.getFormat() == null ? MediaFormat.PNG : request.getFormat());
                dto.setStatus(ResponseStatus.SUCCESS);
                return dto;
            }
            return failed(dto, "Gemini no devolvió datos de imagen. Respuesta: " + response.body());
        } catch (Exception e) { return failed(dto, "No se pudo interpretar la respuesta de imagen de Gemini: " + e.getMessage()); }
    }

    /**
     * Selecciona el proveedor de audio y delega la generación.
     *
     * <p>Proveedores soportados:</p>
     * <ul>
     *   <li>{@code GEMINI} — Gemini Text-to-Speech (valor por defecto).</li>
     *   <li>{@code CLOUDFLARE} — Deepgram Aura en Cloudflare Workers AI, con voz en español.</li>
     *   <li>{@code ELEVENLABS} — ElevenLabs Multilingual v2.</li>
     * </ul>
     *
     * @param request parámetros de generación
     * @return respuesta del proveedor seleccionado
     */
    private AIResponseDTO generateAudio(PromptRequestDTO request) {
        String provider = request.getAudioProvider() == null ? "" : request.getAudioProvider().trim().toUpperCase();
        return switch (provider) {
            case "", "GEMINI" -> generateGeminiAudio(request);
            case "CLOUDFLARE" -> generateCloudflareAudio(request);
            case "ELEVENLABS" -> generateElevenLabsAudio(request);
            default -> failed(base(AIProvider.GEMINI, "audio-provider", 400,
                    System.currentTimeMillis()), "Proveedor de audio no valido: " + provider);
        };
    }

    /**
     * Genera voz con Deepgram Aura en Cloudflare Workers AI. Intenta primero la voz
     * en español ({@code aura-2-es}) y, si no está disponible, usa {@code aura-1}.
     * Reutiliza la key {@code ACCOUNT_ID|API_TOKEN} de Cloudflare.
     *
     * @param request parámetros de generación
     * @return respuesta con el audio generado o el detalle del error
     */
    private AIResponseDTO generateCloudflareAudio(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String credentials = optionalKey(AIProvider.CLOUDFLARE);
        String[] parts = credentials == null ? new String[0] : credentials.split("\\|", 2);
        if (!validCloudflareCredentials(parts)) {
            return failed(base(AIProvider.CLOUDFLARE, "@cf/deepgram/aura-2-es", 400, start),
                    "Cloudflare key inválida. Configura ACCOUNT_ID|API_TOKEN en el panel de administración.");
        }
        String accountId = parts[0].trim();
        String token = parts[1].trim();
        JsonObject body = new JsonObject();
        body.addProperty("text", request.getPromptText());
        Map<String, String> headers = Map.of(CONTENT_TYPE, APPLICATION_JSON, AUTHORIZATION, BEARER + token);
        AIResponseDTO last = null;
        for (String model : List.of("@cf/deepgram/aura-2-es", "@cf/deepgram/aura-1")) {
            try {
                HttpResponse<byte[]> response = externalHandler
                        .doPostBytesAsync(CLOUDFLARE_BASE + accountId + "/ai/run/" + model, headers, body.toString())
                        .join();
                AIResponseDTO dto = base(AIProvider.CLOUDFLARE, model, response.statusCode(), start);
                if (!ok(response.statusCode())) {
                    last = failed(dto, "Cloudflare: " + bytesError(response));
                    continue;
                }
                AIResponseDTO audio = audioFromResponse(dto, response, start);
                if (ResponseStatus.SUCCESS.equals(audio.getStatus())) return audio;
                last = audio;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Cloudflare audio generation failed", e);
                last = failed(base(AIProvider.CLOUDFLARE, model, 500, start), "Error Cloudflare: " + e.getMessage());
            }
        }
        return last;
    }

    /**
     * Genera voz con ElevenLabs usando el modelo multilingüe, que pronuncia bien el español.
     *
     * @param request parámetros de generación
     * @return respuesta con el audio generado o el detalle del error
     */
    private AIResponseDTO generateElevenLabsAudio(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String model = "eleven_multilingual_v2";
        String apiKey = optionalKey(AIProvider.ELEVENLABS);
        if (apiKey == null) {
            return failed(base(AIProvider.ELEVENLABS, model, 400, start),
                    "Configura la API key de ElevenLabs en el panel de administración.");
        }
        try {
            JsonObject body = new JsonObject();
            body.addProperty("text", request.getPromptText());
            body.addProperty("model_id", model);
            String voiceId = "JBFqnCBsd6RMkjVDRZzb";
            HttpResponse<byte[]> response = externalHandler.doPostBytesAsync(
                    "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "?output_format=mp3_44100_128",
                    Map.of(CONTENT_TYPE, APPLICATION_JSON, "Accept", "audio/mpeg", "xi-api-key", apiKey),
                    body.toString()).join();
            AIResponseDTO dto = base(AIProvider.ELEVENLABS, model, response.statusCode(), start);
            if (!ok(response.statusCode())) return failed(dto, "ElevenLabs: " + bytesError(response));
            return audioFromResponse(dto, response, start);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "ElevenLabs audio generation failed", e);
            return failed(base(AIProvider.ELEVENLABS, model, 500, start), "Error ElevenLabs: " + e.getMessage());
        }
    }

    /**
     * Convierte una respuesta de audio en un resultado exitoso. Acepta audio binario
     * o JSON con el audio en base64 o como URL en {@code result.audio}.
     *
     * @param dto respuesta que se actualizará
     * @param response respuesta HTTP del proveedor
     * @param start instante en que inició la generación
     * @return respuesta con el audio o con el detalle del fallo
     */
    private AIResponseDTO audioFromResponse(AIResponseDTO dto, HttpResponse<byte[]> response, long start) {
        String contentType = response.headers().firstValue(CONTENT_TYPE).orElse("audio/mpeg");
        if (contentType.contains("json")) {
            JsonObject json = JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject result = json.has("result") && json.get("result").isJsonObject() ? json.getAsJsonObject("result") : json;
            String audio = stringValue(result, "audio");
            if (audio == null || audio.isBlank()) return failed(dto, "El proveedor no devolvió audio: " + json);
            dto.setDownloadUrl(audio.startsWith("http") ? audio : "data:audio/mpeg;base64," + audio);
        } else {
            dto.setDownloadUrl("data:audio/mpeg;base64," + Base64.getEncoder().encodeToString(response.body()));
        }
        dto.setMimeType("audio/mpeg");
        dto.setFormat(MediaFormat.MP3);
        dto.setStatus(ResponseStatus.SUCCESS);
        dto.setDurationMs(System.currentTimeMillis() - start);
        return dto;
    }

    /**
     * Genera audio hablado a partir del texto solicitado mediante Gemini.
     *
     * @param request parámetros de generación
     * @return respuesta con el audio generado o el detalle del error
     */
    private AIResponseDTO generateGeminiAudio(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String key;
        try { key = key(AIProvider.GEMINI); }
        catch (Exception e) { return failed(base(AIProvider.GEMINI, "gemini-2.5-flash-preview-tts", 400, start), "Configura la API key de Gemini en el panel de administración."); }
        String model = "gemini-2.5-flash-preview-tts";

        JsonObject body = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Read this text aloud naturally in Spanish: " + request.getPromptText());
        parts.add(text);

        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        body.add("contents", contents);

        JsonObject config = new JsonObject();
        JsonArray modalities = new JsonArray();
        modalities.add("AUDIO");
        config.add("responseModalities", modalities);

        JsonObject speech = new JsonObject();
        JsonObject voiceConfig = new JsonObject();
        JsonObject prebuilt = new JsonObject();
        prebuilt.addProperty("voiceName", "Kore");
        voiceConfig.add("prebuiltVoiceConfig", prebuilt);
        speech.add("voiceConfig", voiceConfig);
        config.add("speechConfig", speech);
        body.add("generationConfig", config);
        HttpResponse<String> response = post(GEMINI_BASE + model + ":generateContent", Map.of("Content-Type", "application/json", "x-goog-api-key", key), body.toString());
        AIResponseDTO dto = base(AIProvider.GEMINI, model, response.statusCode(), start);
        if (!ok(response)) return failed(dto, error(response));
        try {
            JsonArray outParts = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts");
            for (var p : outParts) if (p.isJsonObject() && p.getAsJsonObject().has("inlineData")) {
                JsonObject data = p.getAsJsonObject().getAsJsonObject("inlineData");
                String mime = data.get("mimeType").getAsString();
                byte[] audioBytes = Base64.getDecoder().decode(data.get("data").getAsString());
                if (mime.toLowerCase().contains("l16") || mime.toLowerCase().contains("pcm")) audioBytes = pcmToWav(audioBytes, 24000, 1, 16);
                String outMime = mime.toLowerCase().contains("l16") || mime.toLowerCase().contains("pcm") ? "audio/wav" : mime;
                dto.setMimeType(outMime);
                dto.setFormat(MediaFormat.WAV);
                dto.setDownloadUrl("data:" + outMime + ";base64," + Base64.getEncoder().encodeToString(audioBytes));
                dto.setStatus(ResponseStatus.SUCCESS);
                return dto;
            }
            return failed(dto, "Gemini no devolvió datos de audio.");
        } catch (Exception e) { return failed(dto, "No se pudo interpretar la respuesta de audio: " + e.getMessage()); }
    }

    /**
     * Selecciona el modelo de texto a video de Hugging Face y delega la generación.
     * Si no se indica un modelo, se usa {@link AIProvider#HF_MINIMAX_H3}.
     *
     * @param request parámetros de generación
     * @return respuesta del modelo seleccionado
     */
    private AIResponseDTO generateVideo(PromptRequestDTO request) {
        String provider = request.getVideoProvider();
        AIProvider hfProvider = provider == null || provider.isBlank() ? AIProvider.HF_MINIMAX_H3 : hfProvider(provider);
        if (hfProvider == null) {
            return failed(base(AIProvider.HF_MINIMAX_H3, "video-provider", 400, System.currentTimeMillis()),
                    "Modelo de video no válido: " + provider);
        }
        return generateSpaceVideo(hfProvider, HF_SPACES.get(hfProvider), request.getPromptText());
    }

    /**
     * Obtiene el proveedor de Hugging Face asociado al identificador recibido.
     *
     * @param provider identificador del proveedor enviado por el cliente
     * @return proveedor configurado o {@code null} si no corresponde a un modelo de Hugging Face
     */
    private AIProvider hfProvider(String provider) {
        try {
            AIProvider value = AIProvider.valueOf(provider.trim().toUpperCase());
            return HF_SPACES.containsKey(value) ? value : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Genera un video mediante un Space de Hugging Face basado en Gradio.
     * Envía la tarea al endpoint configurado del Space y espera su resultado.
     *
     * @param provider proveedor cuya API key se usará para autenticar la solicitud
     * @param config configuración del Space
     * @param prompt texto que describe el video
     * @return respuesta con la URL del video generado o el detalle del error
     */
    private AIResponseDTO generateSpaceVideo(AIProvider provider, HfSpace config, String prompt) {
        long start = System.currentTimeMillis();
        String space = config.space();
        String token;
        try {
            token = key(provider);
        } catch (Exception e) {
            return failed(base(provider, space, 400, start),
                    "Configura la API key de " + provider.getDisplayName()
                    + " en el panel de administración o en Swagger (/admin/apikey/update/" + provider + ").");
        }
        try {
            String host = "https://" + space.toLowerCase().replace("/", "-").replace("_", "-").replace(".", "-") + ".hf.space";
            String endpoint = host + "/gradio_api/call/" + config.apiName();

            JsonObject body = new JsonObject();
            body.add("data", config.data().apply(prompt));

            Map<String, String> headers = Map.of(
                    "Authorization", "Bearer " + token,
                    "Content-Type", "application/json");

            HttpResponse<String> submit = post(endpoint, headers, body.toString());
            AIResponseDTO dto = base(provider, space, submit.statusCode(), start);
            if (!ok(submit)) {
                LOGGER.warning("Hugging Face submit falló: HTTP " + submit.statusCode() + " " + submit.body());
                return failed(dto, "Hugging Face: " + error(submit));
            }
            String eventId = stringValue(JsonParser.parseString(submit.body()).getAsJsonObject(), "event_id");
            if (eventId == null) return failed(dto, "Hugging Face no devolvió event_id: " + submit.body());

            HttpResponse<String> result = get(endpoint + "/" + enc(eventId), Map.of("Authorization", "Bearer " + token));
            if (!ok(result)) return failed(dto, "Hugging Face resultado: " + error(result));
            return parseGradioStream(result.body(), dto, start, token);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Hugging Face video generation failed", e);
            return failed(base(provider, space, 500, start), "Error Hugging Face: " + e.getMessage());
        }
    }

    /**
     * Interpreta la respuesta en formato de eventos de Gradio y extrae la URL del video.
     *
     * @param stream cuerpo de la respuesta del Space
     * @param dto respuesta que se actualizará con el resultado
     * @param start instante en que inició la generación
     * @param token token de Hugging Face, necesario para descargar el video del Space
     * @return respuesta con el video generado o con el detalle del fallo
     */
    private AIResponseDTO parseGradioStream(String stream, AIResponseDTO dto, long start, String token) {
        String event = null;
        for (String line : stream.split("\\r?\\n")) {
            if (line.startsWith("event:")) {
                event = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                String payload = line.substring(5).trim();
                if ("error".equals(event)) {
                    LOGGER.warning("Hugging Face Space devolvió error: " + payload);
                    return failed(dto, "El Space falló (puede estar saturado o sin cuota de GPU): " + payload);
                }
                if ("complete".equals(event)) {
                    String url = findUrl(JsonParser.parseString(payload));
                    if (url == null) return failed(dto, "El Space terminó sin URL de video: " + payload);
                    return successVideo(dto, url, start, token);
                }
            }
        }
        return failed(dto, "Respuesta inesperada del Space: " + stream);
    }

    /**
     * Busca de forma recursiva la primera URL dentro de la salida de Gradio.
     *
     * @param el elemento JSON que se va a inspeccionar
     * @return URL encontrada o {@code null} si no existe
     */
    private String findUrl(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            String url = stringValue(o, "url");
            if (url != null && url.startsWith("http")) return url;
            for (var entry : o.entrySet()) {
                String found = findUrl(entry.getValue());
                if (found != null) return found;
            }
        } else if (el.isJsonArray()) {
            for (var item : el.getAsJsonArray()) {
                String found = findUrl(item);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Descarga el video generado por el Space y lo devuelve como data URL, igual que las imágenes.
     *
     * <p>Los Spaces de Gradio no siempre permiten abrir el archivo desde el navegador
     * ("File not allowed"), así que el backend lo descarga con el token y lo reenvía al usuario
     * sin guardarlo en la base de datos. Se prueban la ruta {@code /gradio_api/file=} y la ruta
     * antigua {@code /file=}.</p>
     *
     * @param dto respuesta que se actualizará con el resultado
     * @param url URL del video en el Space
     * @param start instante en que inició la generación
     * @param token token de Hugging Face
     * @return respuesta con el video embebido o con el detalle del fallo
     */
    private AIResponseDTO successVideo(AIResponseDTO dto, String url, long start, String token) {
        Map<String, String> headers = Map.of(AUTHORIZATION, BEARER + token);
        HttpResponse<byte[]> video = null;
        for (String candidato : List.of(url, url.replace("/gradio_api/file=", "/file=")).stream().distinct().toList()) {
            video = externalHandler.doGetBytesAsync(candidato, headers).join();
            if (ok(video.statusCode())) break;
        }
        if (video == null || !ok(video.statusCode())) {
            LOGGER.warning("No se pudo descargar el video del Space: " + url);
            return failed(dto, "El Space generó el video pero no permite descargarlo ("
                    + (video == null ? "sin respuesta" : bytesError(video)) + "). Prueba con otro modelo de video.");
        }
        dto.setStatus(ResponseStatus.SUCCESS);
        dto.setDownloadUrl("data:video/mp4;base64," + Base64.getEncoder().encodeToString(video.body()));
        dto.setMimeType("video/mp4");
        dto.setFormat(MediaFormat.MP4);
        dto.setDurationMs(System.currentTimeMillis() - start);
        return dto;
    }

    /** Obtiene un campo como texto, o null si no existe o no puede convertirse. */
    private String stringValue(JsonObject object, String field) {
        if (object == null || !object.has(field) || object.get(field).isJsonNull()) return null;
        try { return object.get(field).getAsString(); } catch (Exception e) { return null; }
    }

    /** Convierte audio PCM sin encabezado en un archivo WAV con sus metadatos. */
    private byte[] pcmToWav(byte[] pcm, int sampleRate, int channels, int bits) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int byteRate = sampleRate * channels * bits / 8;
            out.write(new byte[] {'R','I','F','F'}); writeInt(out, 36 + pcm.length); out.write(new byte[] {'W','A','V','E','f','m','t',' '});
            writeInt(out, 16); writeShort(out, 1); writeShort(out, channels); writeInt(out, sampleRate); writeInt(out, byteRate); writeShort(out, channels * bits / 8); writeShort(out, bits);
            out.write(new byte[] {'d','a','t','a'}); writeInt(out, pcm.length); out.write(pcm); return out.toByteArray();
        } catch (IOException e) { return pcm; }
    }
    /** Escribe un entero de 32 bits en formato little-endian. */
    private void writeInt(ByteArrayOutputStream out, int value) { out.write(value & 255); out.write((value >>> 8) & 255); out.write((value >>> 16) & 255); out.write((value >>> 24) & 255); }
    /** Escribe un entero de 16 bits en formato little-endian. */
    private void writeShort(ByteArrayOutputStream out, int value) { out.write(value & 255); out.write((value >>> 8) & 255); }

    /** Recupera la API key del proveedor o lanza un error de configuración. */
    private String key(AIProvider provider) {
        try {
            return apiKeyService.getDecryptedKey(provider);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Configura la API key de " + provider.getDisplayName()
                    + " en Swagger: /admin/apikey/update/" + provider, e);
        }
    }

    /** Ejecuta una solicitud POST y espera su respuesta. */
    private HttpResponse<String> post(String url, Map<String, String> headers, String body) {
        return externalHandler.doPostAsync(url, headers, body).join();
    }

    /**
     * Recupera la API key de un proveedor si está configurada.
     *
     * @param provider proveedor cuya key se busca
     * @return key descifrada o {@code null} si no está configurada
     */
    private String optionalKey(AIProvider provider) {
        try {
            String value = apiKeyService.getDecryptedKey(provider);
            return value == null || value.isBlank() ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Indica si un código HTTP representa una respuesta exitosa.
     *
     * @param status código HTTP
     * @return {@code true} si está entre 200 y 299
     */
    private boolean ok(int status) {
        return status >= 200 && status < 300;
    }

    /**
     * Extrae un mensaje de error de una respuesta con cuerpo binario.
     *
     * @param response respuesta HTTP del proveedor
     * @return mensaje de error legible
     */
    private String bytesError(HttpResponse<byte[]> response) {
        String text = new String(response.body(), StandardCharsets.UTF_8);
        try {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            if (json.has("detail") && json.get("detail").isJsonObject()) {
                String message = stringValue(json.getAsJsonObject("detail"), "message");
                if (message != null) return message;
            }
            if (json.has("errors") && json.get("errors").isJsonArray() && !json.getAsJsonArray("errors").isEmpty()
                    && json.getAsJsonArray("errors").get(0).isJsonObject()) {
                String message = stringValue(json.getAsJsonArray("errors").get(0).getAsJsonObject(), "message");
                if (message != null) return message;
            }
            String message = stringValue(json, "error");
            if (message != null) return message;
            message = stringValue(json, "message");
            if (message != null) return message;
        } catch (Exception ignored) {
            LOGGER.log(Level.FINE, "La respuesta de error no es JSON");
        }
        String shortText = text.length() > 300 ? text.substring(0, 300) : text;
        return "HTTP " + response.statusCode() + ": " + shortText;
    }

    /** Ejecuta una solicitud GET y espera su respuesta. */
    private HttpResponse<String> get(String url, Map<String, String> headers) {
        return externalHandler.doGetAsync(url, headers).join();
    }

    /** Indica si el código HTTP representa una respuesta exitosa. */
    private boolean ok(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    /** Extrae un mensaje de error del cuerpo HTTP, si existe. */
    private String error(HttpResponse<String> response) {
        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("error")) {
                JsonObject errorObject = json.get("error").isJsonObject()
                        ? json.getAsJsonObject("error") : null;
                if (errorObject != null && errorObject.has("message")) {
                    return errorObject.get("message").getAsString();
                }
                return json.get("error").toString();
            }
            if (json.has("message")) {
                return json.get("message").getAsString();
            }
        } catch (Exception parseException) {
            LOGGER.log(Level.FINE, "La respuesta HTTP no contiene JSON de error", parseException);
        }
        return "HTTP " + response.statusCode() + ": " + response.body();
    }

    /** Codifica un valor para usarlo en un parámetro URL. */
    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Crea una respuesta base con metadatos del proveedor y duración. */
    private AIResponseDTO base(AIProvider provider, String model, int status, long start) {
        AIResponseDTO dto = new AIResponseDTO();
        dto.setProvider(provider);
        dto.setModel(model);
        dto.setHttpStatus(status);
        dto.setDurationMs(System.currentTimeMillis() - start);
        return dto;
    }

    /** Marca una respuesta como fallida y agrega el detalle del error. */
    private AIResponseDTO failed(AIResponseDTO dto, String message) {
        dto.setStatus(ResponseStatus.FAILED);
        dto.setErrorMessage(message == null ? "Error sin detalle" : message);
        return dto;
    }
}