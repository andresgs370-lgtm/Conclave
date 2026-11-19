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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.edu.unbosque.conclave.dto.AIResponseDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;
import co.edu.unbosque.conclave.exception.ApiKeyNotConfiguredException;
import co.edu.unbosque.conclave.exception.UnsupportedOutputTypeException;
import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.MediaFormat;
import co.edu.unbosque.conclave.model.OutputType;
import co.edu.unbosque.conclave.model.ResponseStatus;

@Service
public class PromptService {
    private static final Logger LOGGER = Logger.getLogger(PromptService.class.getName());
    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String AGNES_BASE = "https://apihub.agnes-ai.com";
    private static final long VIDEO_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(10);
    private static final long POLL_MS = TimeUnit.SECONDS.toMillis(5);

    @Autowired private ExternalHTTPRequestHandler externalHandler;
    @Autowired private ApiKeyService apiKeyService;

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

    private AIResponseDTO generateImage(PromptRequestDTO request) {
        String provider = request.getImageProvider();
        if (provider != null && "GEMINI".equalsIgnoreCase(provider.trim())) return generateGeminiImage(request);
        if (provider != null && !provider.isBlank() && !"CLOUDFLARE".equalsIgnoreCase(provider.trim())) {
            return failed(base(AIProvider.CLOUDFLARE, "image-provider", 400, System.currentTimeMillis()), "Proveedor de imagen no válido: " + provider);
        }
        long start = System.currentTimeMillis();
        String credentials = key(AIProvider.CLOUDFLARE);
        String[] parts = credentials.split("\\|", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
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
            String imageData = json.has("image") && !json.get("image").isJsonNull() ? json.get("image").getAsString() : (json.has("result") && json.get("result").isJsonObject() && json.getAsJsonObject("result").has("image") ? json.getAsJsonObject("result").get("image").getAsString() : null);
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


    private AIResponseDTO generateGeminiImage(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String apiKey;
        try { apiKey = key(AIProvider.GEMINI); }
        catch (Exception e) { return failed(base(AIProvider.GEMINI, "gemini-2.5-flash-image", 400, start), "Configura la API Key de Gemini en Swagger."); }
        String model = "gemini-2.5-flash-image";
        JsonObject body = new JsonObject(); JsonArray parts = new JsonArray(); JsonObject prompt = new JsonObject(); prompt.addProperty("text", request.getPromptText()); parts.add(prompt);
        JsonObject content = new JsonObject(); content.add("parts", parts); JsonArray contents = new JsonArray(); contents.add(content); body.add("contents", contents);
        JsonObject generationConfig = new JsonObject(); JsonArray modalities = new JsonArray(); modalities.add("TEXT"); modalities.add("IMAGE"); generationConfig.add("responseModalities", modalities); body.add("generationConfig", generationConfig);
        HttpResponse<String> response = post(GEMINI_BASE + model + ":generateContent", Map.of("Content-Type", "application/json", "x-goog-api-key", apiKey), body.toString());
        AIResponseDTO dto = base(AIProvider.GEMINI, model, response.statusCode(), start);
        if (!ok(response)) return failed(dto, error(response));
        try {
            JsonArray outParts = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts");
            for (var part : outParts) if (part.isJsonObject() && part.getAsJsonObject().has("inlineData")) {
                JsonObject data = part.getAsJsonObject().getAsJsonObject("inlineData"); String mime = data.has("mimeType") ? data.get("mimeType").getAsString() : "image/png";
                String base64 = data.get("data").getAsString(); dto.setMimeType(mime); dto.setDownloadUrl("data:" + mime + ";base64," + base64); dto.setFormat(request.getFormat() == null ? MediaFormat.PNG : request.getFormat()); dto.setStatus(ResponseStatus.SUCCESS); return dto;
            }
            return failed(dto, "Gemini no devolvió datos de imagen. Respuesta: " + response.body());
        } catch (Exception e) { return failed(dto, "No se pudo interpretar la respuesta de imagen de Gemini: " + e.getMessage()); }
    }

    private AIResponseDTO generateAudio(PromptRequestDTO request) {
        long start = System.currentTimeMillis(); String key = key(AIProvider.GEMINI); String model = "gemini-2.5-flash-preview-tts";
        JsonObject body = new JsonObject(); JsonArray parts = new JsonArray(); JsonObject text = new JsonObject(); text.addProperty("text", "Read this text aloud naturally in Spanish: " + request.getPromptText()); parts.add(text);
        JsonObject content = new JsonObject(); content.add("parts", parts); JsonArray contents = new JsonArray(); contents.add(content); body.add("contents", contents);
        JsonObject config = new JsonObject(); JsonArray modalities = new JsonArray(); modalities.add("AUDIO"); config.add("responseModalities", modalities);
        JsonObject speech = new JsonObject(); JsonObject voiceConfig = new JsonObject(); JsonObject prebuilt = new JsonObject(); prebuilt.addProperty("voiceName", "Kore"); voiceConfig.add("prebuiltVoiceConfig", prebuilt); speech.add("voiceConfig", voiceConfig); config.add("speechConfig", speech); body.add("generationConfig", config);
        HttpResponse<String> response = post(GEMINI_BASE + model + ":generateContent", Map.of("Content-Type", "application/json", "x-goog-api-key", key), body.toString());
        AIResponseDTO dto = base(AIProvider.GEMINI, model, response.statusCode(), start);
        if (!ok(response)) return failed(dto, error(response));
        try {
            JsonArray outParts = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts");
            for (var p : outParts) if (p.isJsonObject() && p.getAsJsonObject().has("inlineData")) {
                JsonObject data = p.getAsJsonObject().getAsJsonObject("inlineData"); String mime = data.get("mimeType").getAsString();
                byte[] audioBytes = Base64.getDecoder().decode(data.get("data").getAsString());
                if (mime.toLowerCase().contains("l16") || mime.toLowerCase().contains("pcm")) audioBytes = pcmToWav(audioBytes, 24000, 1, 16);
                String outMime = mime.toLowerCase().contains("l16") || mime.toLowerCase().contains("pcm") ? "audio/wav" : mime;
                dto.setMimeType(outMime); dto.setFormat(MediaFormat.WAV); dto.setDownloadUrl("data:" + outMime + ";base64," + Base64.getEncoder().encodeToString(audioBytes)); dto.setStatus(ResponseStatus.SUCCESS); return dto;
            }
            return failed(dto, "Gemini no devolvió datos de audio.");
        } catch (Exception e) { return failed(dto, "No se pudo interpretar la respuesta de audio: " + e.getMessage()); }
    }

    private AIResponseDTO generateVideo(PromptRequestDTO request) {
        String provider = request.getVideoProvider();
        if (provider == null || provider.isBlank() || "AGNES_AI".equalsIgnoreCase(provider)) return generateAgnesVideo(request);
        if ("KINOVI_AI".equalsIgnoreCase(provider)) return generateKinoviVideo(request);
        if ("FAL_AI".equalsIgnoreCase(provider)) return generateFalVideo(request);
        if ("REPLICATE".equalsIgnoreCase(provider)) return generateReplicateVideo(request);
        if ("HUGGINGFACE".equalsIgnoreCase(provider)) return failed(base(AIProvider.HUGGINGFACE, request.getVideoModel(), 501, System.currentTimeMillis()), "Hugging Face Spaces no tiene un endpoint de video universal. Debes configurar el Space/API específico; esta opción aún no está implementada como adaptador genérico.");
        return failed(base(AIProvider.AGNES_AI, "video-provider", 400, System.currentTimeMillis()), "Proveedor de video no válido: " + provider);
    }

    private AIResponseDTO generateAgnesVideo(PromptRequestDTO request) {
        long start = System.currentTimeMillis(); String model = "agnes-video-2.5-flash"; String apiKey;
        try { apiKey = key(AIProvider.AGNES_AI); }
        catch (Exception e) { return failed(base(AIProvider.AGNES_AI, model, 400, start), "Configura la API Key de Agnes AI en Swagger: /admin/apikey/update/AGNES_AI."); }
        try {
            JsonObject payload = new JsonObject(); payload.addProperty("model", model); payload.addProperty("prompt", request.getPromptText()); payload.addProperty("mode", "text"); payload.addProperty("seconds", "5"); payload.addProperty("size", "720P"); payload.addProperty("aspect_ratio", "16:9");
            HttpResponse<String> create = post(AGNES_BASE + "/v1/videos", Map.of("Authorization", "Bearer " + apiKey, "Content-Type", "application/json"), payload.toString());
            AIResponseDTO dto = base(AIProvider.AGNES_AI, model, create.statusCode(), start); if (!ok(create)) return failed(dto, "Agnes AI: " + error(create));
            JsonObject created = JsonParser.parseString(create.body()).getAsJsonObject(); String videoId = firstString(created, "video_id", "id", "task_id");
            if (videoId == null) return failed(dto, "Agnes AI no devolvió video_id: " + create.body());
            return pollAgnesVideo(videoId, model, apiKey, dto, start, System.currentTimeMillis() + VIDEO_TIMEOUT_MS);
        } catch (Exception e) { LOGGER.log(Level.WARNING, "Agnes AI video generation failed", e); return failed(base(AIProvider.AGNES_AI, model, 500, start), "Error Agnes AI: " + e.getMessage()); }
    }

    private AIResponseDTO generateKinoviVideo(PromptRequestDTO request) {
        long start = System.currentTimeMillis(); String model = modelOr(request.getVideoModel(), "seedance2-5"); String apiKey;
        try { apiKey = key(AIProvider.KINOVI_AI); } catch (Exception e) { return failed(base(AIProvider.KINOVI_AI, model, 400, start), "Configura KINOVI_AI en Swagger (/admin/apikey/update/KINOVI_AI)."); }
        try {
            JsonObject inputs = new JsonObject(); inputs.addProperty("prompt", request.getPromptText()); inputs.addProperty("duration", 5); inputs.addProperty("outputResolution", "720p");
            JsonObject body = new JsonObject(); body.addProperty("model", model); body.add("inputs", inputs);
            HttpResponse<String> created = post("https://kinovi.ai/api/v1/jobs/createTask", Map.of("Authorization", "Bearer " + apiKey, "Content-Type", "application/json"), body.toString());
            AIResponseDTO dto = base(AIProvider.KINOVI_AI, model, created.statusCode(), start); if (!ok(created)) return failed(dto, "Kinovi: " + error(created));
            String taskId = stringValue(JsonParser.parseString(created.body()).getAsJsonObject(), "taskId"); if (taskId == null) return failed(dto, "Kinovi no devolvió taskId: " + created.body());
            return pollKinovi(taskId, model, apiKey, dto, start, System.currentTimeMillis()+VIDEO_TIMEOUT_MS);
        } catch (Exception e) { return failed(base(AIProvider.KINOVI_AI, model, 500, start), "Error Kinovi: " + e.getMessage()); }
    }

    /** Consulta el estado de una tarea de video de Kinovi hasta finalizar o agotar el tiempo límite. */
    private AIResponseDTO pollKinovi(String taskId, String model, String apiKey, AIResponseDTO dto, long start, long deadline) throws Exception {
        if (System.currentTimeMillis() > deadline) {
            return failed(dto, "Kinovi excedió el tiempo máximo de generación.");
        }

        HttpResponse<String> response = get(
                "https://kinovi.ai/api/v1/jobs/recordInfo?taskId=" + enc(taskId),
                Map.of("Authorization", "Bearer " + apiKey));
        if (!ok(response)) {
            return failed(dto, "Kinovi: " + error(response));
        }

        JsonObject info = JsonParser.parseString(response.body()).getAsJsonObject();
        String status = stringValue(info, "status");
        if ("success".equalsIgnoreCase(status)) {
            String url = outputUrl(info);
            if (url == null) {
                return failed(dto, "Kinovi terminó sin URL de video: " + response.body());
            }
            return successVideo(dto, url, start);
        }
        if ("fail".equalsIgnoreCase(status)) {
            String message = firstString(info, "message", "error");
            return failed(dto, message == null ? "Kinovi falló: " + response.body() : message);
        }

        Thread.sleep(POLL_MS);
        return pollKinovi(taskId, model, apiKey, dto, start, deadline);
    }

    /** Envía una solicitud de generación de video a fal.ai y procesa su tarea asíncrona. */
    private AIResponseDTO generateFalVideo(PromptRequestDTO request) {
        long start = System.currentTimeMillis();
        String model = modelOr(request.getVideoModel(), "fal-ai/wan/v2.1/1.3b/text-to-video");
        String apiKey;
        try {
            apiKey = key(AIProvider.FAL_AI);
        } catch (Exception e) {
            return failed(base(AIProvider.FAL_AI, model, 400, start),
                    "Configura FAL_AI en Swagger (/admin/apikey/update/FAL_AI).");
        }

        try {
            JsonObject body = new JsonObject();
            body.addProperty("prompt", request.getPromptText());
            HttpResponse<String> submit = post(
                    "https://queue.fal.run/" + model,
                    Map.of("Authorization", "Key " + apiKey, "Content-Type", "application/json"),
                    body.toString());
            AIResponseDTO dto = base(AIProvider.FAL_AI, model, submit.statusCode(), start);
            if (!ok(submit)) {
                return failed(dto, "fal.ai: " + error(submit));
            }

            JsonObject json = JsonParser.parseString(submit.body()).getAsJsonObject();
            String requestId = stringValue(json, "request_id");
            String statusUrl = stringValue(json, "status_url");
            String resultUrl = stringValue(json, "response_url");
            if (requestId == null || statusUrl == null || resultUrl == null) {
                return failed(dto, "fal.ai no devolvió request_id/status_url/response_url: " + submit.body());
            }
            long deadline = System.currentTimeMillis() + VIDEO_TIMEOUT_MS;
            return pollFal(requestId, statusUrl, resultUrl, model, apiKey, dto, start, deadline);
        } catch (Exception e) {
            return failed(base(AIProvider.FAL_AI, model, 500, start), "Error fal.ai: " + e.getMessage());
        }
    }

    /** Consulta el estado de una tarea de fal.ai y obtiene el resultado cuando termina. */
    private AIResponseDTO pollFal(String id, String statusUrl, String resultUrl, String model, String apiKey, AIResponseDTO dto, long start, long deadline) throws Exception {
        if (System.currentTimeMillis() > deadline) {
            return failed(dto, "fal.ai excedió el tiempo máximo de generación.");
        }

        HttpResponse<String> status = get(statusUrl, Map.of("Authorization", "Key " + apiKey));
        if (!ok(status)) {
            return failed(dto, "fal.ai status: " + error(status));
        }

        JsonObject stat = JsonParser.parseString(status.body()).getAsJsonObject();
        String state = stringValue(stat, "status");
        if ("COMPLETED".equalsIgnoreCase(state)) {
            HttpResponse<String> result = get(resultUrl, Map.of("Authorization", "Key " + apiKey));
            if (!ok(result)) {
                return failed(dto, "fal.ai result: " + error(result));
            }
            String url = outputUrl(JsonParser.parseString(result.body()).getAsJsonObject());
            if (url == null) {
                return failed(dto, "fal.ai no devolvió URL de video: " + result.body());
            }
            return successVideo(dto, url, start);
        }
        if ("FAILED".equalsIgnoreCase(state)) {
            return failed(dto, "fal.ai falló: " + status.body());
        }

        Thread.sleep(POLL_MS);
        return pollFal(id, statusUrl, resultUrl, model, apiKey, dto, start, deadline);
    }

    private AIResponseDTO generateReplicateVideo(PromptRequestDTO request) {
        long start = System.currentTimeMillis(); String model = modelOr(request.getVideoModel(), "minimax/video-01"); String apiKey;
        try { apiKey = key(AIProvider.REPLICATE); } catch (Exception e) { return failed(base(AIProvider.REPLICATE, model, 400, start), "Configura REPLICATE en Swagger (/admin/apikey/update/REPLICATE)."); }
        try {
            JsonObject input = new JsonObject(); input.addProperty("prompt", request.getPromptText()); JsonObject body = new JsonObject(); body.add("input", input);
            HttpResponse<String> created = post("https://api.replicate.com/v1/models/" + model + "/predictions", Map.of("Authorization", "Bearer " + apiKey, "Content-Type", "application/json", "Prefer", "wait"), body.toString());
            AIResponseDTO dto = base(AIProvider.REPLICATE, model, created.statusCode(), start); if (!ok(created)) return failed(dto, "Replicate: " + error(created));
            JsonObject prediction = JsonParser.parseString(created.body()).getAsJsonObject(); String id = stringValue(prediction, "id"); String pollUrl = prediction.has("urls") && prediction.get("urls").isJsonObject() ? stringValue(prediction.getAsJsonObject("urls"), "get") : null;
            String url = outputUrl(prediction); String state = stringValue(prediction, "status"); if (url != null && List.of("succeeded", "success").contains(state)) return successVideo(dto, url, start);
            if (pollUrl == null && id != null) pollUrl = "https://api.replicate.com/v1/predictions/" + id;
            if (pollUrl == null) return failed(dto, "Replicate no devolvió URL de consulta: " + created.body()); return pollReplicate(pollUrl, model, apiKey, dto, start, System.currentTimeMillis()+VIDEO_TIMEOUT_MS);
        } catch (Exception e) { return failed(base(AIProvider.REPLICATE, model, 500, start), "Error Replicate: " + e.getMessage()); }
    }

    private AIResponseDTO pollReplicate(String url, String model, String apiKey, AIResponseDTO dto, long start, long deadline) throws Exception {
        if (System.currentTimeMillis() > deadline) return failed(dto, "Replicate excedió el tiempo máximo de generación."); HttpResponse<String> response = get(url, Map.of("Authorization", "Bearer " + apiKey)); if (!ok(response)) return failed(dto, "Replicate: " + error(response)); JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject(); String status = stringValue(json, "status");
        if ("succeeded".equalsIgnoreCase(status)) { String video = outputUrl(json); if (video == null) return failed(dto, "Replicate terminó sin URL de video: " + response.body()); return successVideo(dto, video, start); }
        if (List.of("failed", "canceled").contains(status)) return failed(dto, firstString(json, "error") == null ? "Replicate falló: " + response.body() : firstString(json, "error")); Thread.sleep(POLL_MS); return pollReplicate(url, model, apiKey, dto, start, deadline);
    }

    private String modelOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
    private String outputUrl(JsonObject json) { String direct = firstString(json, "video_url", "url", "download_url"); if (direct != null) return direct; if (json.has("video") && json.get("video").isJsonObject()) return firstString(json.getAsJsonObject("video"), "url"); if (json.has("output") && json.get("output").isJsonArray() && json.getAsJsonArray("output").size()>0) { var item=json.getAsJsonArray("output").get(0); if(item.isJsonPrimitive()) return item.getAsString(); if(item.isJsonObject()) return firstString(item.getAsJsonObject(), "url"); } if (json.has("output") && json.get("output").isJsonPrimitive()) return json.get("output").getAsString(); if (json.has("data") && json.get("data").isJsonObject()) return outputUrl(json.getAsJsonObject("data")); return null; }
    private AIResponseDTO successVideo(AIResponseDTO dto, String url, long start) { dto.setStatus(ResponseStatus.SUCCESS); dto.setDownloadUrl(url); dto.setMimeType("video/mp4"); dto.setFormat(MediaFormat.MP4); dto.setDurationMs(System.currentTimeMillis()-start); return dto; }

    /**
     * Consulta el estado de la generación de Agnes AI hasta que finaliza o se agota el plazo.
     *
     * @param videoId identificador de la tarea de video
     * @param model modelo utilizado para generar el video
     * @param apiKey clave de autenticación del proveedor
     * @param dto respuesta que se actualizará con el resultado
     * @param start instante en que inició la generación
     * @param deadline instante límite para completar la consulta
     * @return respuesta con el video generado o con el detalle del fallo
     * @throws Exception si ocurre un error durante la consulta o espera
     */
    private AIResponseDTO pollAgnesVideo(String videoId, String model, String apiKey, AIResponseDTO dto, long start, long deadline) throws Exception {
        if (System.currentTimeMillis() > deadline) {
            return failed(dto, "Agnes AI excedió el tiempo máximo de generación de video.");
        }

        String url = AGNES_BASE + "/agnesapi?video_id=" + enc(videoId) + "&model_name=" + enc(model);
        HttpResponse<String> response = get(url, Map.of("Authorization", "Bearer " + apiKey));
        if (!ok(response)) {
            return failed(dto, "Error consultando Agnes AI: " + error(response));
        }

        JsonObject result = JsonParser.parseString(response.body()).getAsJsonObject();
        String status = agnesStatus(result);
        if (status == null) {
            return failed(dto, "Agnes AI devolvió una respuesta sin estado: " + response.body());
        }
        if (isAgnesCompleted(status)) {
            return completeAgnesVideo(result, dto, start, response.body());
        }
        if (isAgnesFailed(status)) {
            return failed(dto, agnesError(result, response.body()));
        }

        Thread.sleep(POLL_MS);
        return pollAgnesVideo(videoId, model, apiKey, dto, start, deadline);
    }

    /** Obtiene el estado de Agnes AI desde la respuesta principal o su objeto data. */
    private String agnesStatus(JsonObject result) {
        String status = stringValue(result, "status");
        if (status == null && result.has("data") && result.get("data").isJsonObject()) {
            status = stringValue(result.getAsJsonObject("data"), "status");
        }
        return status == null ? null : status.toLowerCase();
    }

    /** Indica si Agnes AI reportó que la generación terminó correctamente. */
    private boolean isAgnesCompleted(String status) {
        return List.of("completed", "complete", "success", "succeeded", "done").contains(status);
    }

    /** Indica si Agnes AI reportó un estado de generación fallido. */
    private boolean isAgnesFailed(String status) {
        return List.of("failed", "failure", "error", "cancelled", "canceled").contains(status);
    }

    /** Construye la respuesta exitosa a partir de la URL del video de Agnes AI. */
    private AIResponseDTO completeAgnesVideo(JsonObject result, AIResponseDTO dto, long start, String responseBody) {
        String videoUrl = agnesVideoUrl(result);
        if (videoUrl == null || videoUrl.isBlank()) {
            return failed(dto, "Agnes AI marcó el video como completado, pero no devolvió una URL. Respuesta: " + responseBody);
        }
        return successVideo(dto, videoUrl, start);
    }

    /** Busca la URL del video en los campos habituales de la respuesta de Agnes AI. */
    private String agnesVideoUrl(JsonObject result) {
        String videoUrl = firstString(result, "url", "video_url", "download_url");
        if (videoUrl != null) {
            return videoUrl;
        }
        if (result.has("result") && result.get("result").isJsonObject()) {
            videoUrl = firstString(result.getAsJsonObject("result"), "url", "video_url", "download_url");
        }
        if (videoUrl == null && result.has("data") && result.get("data").isJsonObject()) {
            videoUrl = firstString(result.getAsJsonObject("data"), "url", "video_url", "download_url");
        }
        return videoUrl;
    }

    /** Obtiene el mensaje de error de Agnes AI o utiliza el cuerpo de la respuesta. */
    private String agnesError(JsonObject result, String responseBody) {
        String message = firstString(result, "error", "message");
        return message == null ? "Agnes AI falló la generación: " + responseBody : message;
    }

    /** Obtiene un campo como texto, o null si no existe o no puede convertirse. */
    private String stringValue(JsonObject object, String field) {
        if (object == null || !object.has(field) || object.get(field).isJsonNull()) return null;
        try { return object.get(field).getAsString(); } catch (Exception e) { return null; }
    }

    /** Devuelve el primer campo textual no vacío de los nombres indicados. */
    private String firstString(JsonObject object, String... fields) {
        for (String field : fields) {
            String value = stringValue(object, field);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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
