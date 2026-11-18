package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.dto.AIResponseDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;
import co.edu.unbosque.conclave.exception.ApiKeyNotConfiguredException;
import co.edu.unbosque.conclave.exception.UnsupportedOutputTypeException;
import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.ResponseStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio encargado del consumo asíncrono de la API de OpenRouter.
 *
 * <p>
 * Recibe un {@link PromptRequestDTO} con el texto del usuario y el tipo de
 * salida deseado ({@code IMAGE}, {@code AUDIO} o {@code VIDEO}), lanza en
 * paralelo las peticiones hacia los presets configurados en OpenRouter y
 * devuelve una lista de {@link AIResponseDTO}, una por cada preset.
 * </p>
 *
 * <p>
 * Los resultados <strong>no se persisten</strong>: viajan directamente desde
 * OpenRouter al frontend. Si el usuario recarga la página, debe solicitar la
 * generación de nuevo.
 * </p>
 *
 * <p>
 * Esta clase no accede a la base de datos bajo ninguna circunstancia. La
 * persistencia del prompt es responsabilidad de otro módulo.
 * </p>
 */
@Service
public class PromptService {

	/** Logger de la clase. */
	private static final Logger LOGGER = Logger.getLogger(PromptService.class.getName());

	/** Presets de OpenRouter para generación de imágenes. */
	private static final List<String> PRESETS_IMAGE = List.of("@preset/conclave-imagen-gpt",
			"@preset/conclave-imagen-gemini", "@preset/conclave-imagen-flux", "@preset/conclave-imagen-seedream");

	/** Presets de OpenRouter para generación de audio. */
	private static final List<String> PRESETS_AUDIO = List.of("@preset/conclave-audio-gemini",
			"@preset/conclave-audio-gpt");

	/** Presets de OpenRouter para generación de video. */
	private static final List<String> PRESETS_VIDEO = List.of("@preset/conclave-video-gemini");

	/**
	 * URL base del endpoint de OpenRouter (inyectada desde application.properties).
	 */
	@Value("${openrouter.api.url}")
	private String openRouterUrl;

	/** Componente para realizar peticiones HTTP asíncronas. */
	@Autowired
	private ExternalHTTPRequestHandler externalHandler;

	/** Servicio para obtener la API key activa de OpenRouter. */
	@Autowired
	private ApiKeyService apiKeyService;

	/** Constructor por defecto. */
	public PromptService() {
	}

	/**
	 * Lanza en paralelo las peticiones a OpenRouter según el tipo de salida
	 * indicado en el request y retorna todas las respuestas cuando terminan.
	 *
	 * <p>
	 * Cada preset se ejecuta en un {@link CompletableFuture} independiente. Si un
	 * preset falla, su respuesta lleva {@link ResponseStatus#FAILED} y el resto de
	 * los presets no se ven afectados.
	 * </p>
	 *
	 * @param request DTO con el texto del prompt, el tipo de salida y el formato
	 * @return lista de {@link AIResponseDTO} con la respuesta de cada preset
	 * @throws ApiKeyNotConfiguredException   si la API key de OpenRouter no está
	 *                                        configurada
	 * @throws UnsupportedOutputTypeException si el {@code outputType} no tiene
	 *                                        presets asignados
	 */
	public List<AIResponseDTO> generar(PromptRequestDTO request)
			throws ApiKeyNotConfiguredException, UnsupportedOutputTypeException {

		String apiKey = apiKeyService.getDecryptedKey(AIProvider.OPENROUTER);
		List<String> presets = resolverPresets(request);

		List<CompletableFuture<AIResponseDTO>> futuros = new ArrayList<>();
		for (String preset : presets) {
			CompletableFuture<AIResponseDTO> futuro = llamarPreset(preset, request, apiKey);
			futuros.add(futuro);
		}

		return futuros.stream().map(CompletableFuture::join).toList();
	}

	/**
	 * Devuelve la lista de presets de OpenRouter que corresponden al
	 * {@code outputType} del request.
	 *
	 * @param request request del usuario
	 * @return lista de IDs de presets a invocar
	 * @throws UnsupportedOutputTypeException si el {@code outputType} no tiene
	 *                                        presets definidos
	 */
	private List<String> resolverPresets(PromptRequestDTO request) throws UnsupportedOutputTypeException {

		return switch (request.getOutputType()) {
		case IMAGE -> PRESETS_IMAGE;
		case AUDIO -> PRESETS_AUDIO;
		case VIDEO -> PRESETS_VIDEO;
		default -> throw new UnsupportedOutputTypeException(AIProvider.OPENROUTER, request.getOutputType());
		};
	}

	/**
	 * Construye y lanza de forma asíncrona la petición HTTP hacia un preset
	 * específico de OpenRouter.
	 *
	 * <p>
	 * El {@link CompletableFuture} resultante nunca lanza excepción hacia afuera:
	 * si la llamada falla, retorna un {@link AIResponseDTO} con
	 * {@link ResponseStatus#FAILED} y el mensaje de error.
	 * </p>
	 *
	 * @param presetId ID del preset de OpenRouter a invocar
	 * @param request  request original del usuario
	 * @param apiKey   API key de OpenRouter en texto plano
	 * @return futuro que resuelve con la respuesta del preset
	 */
	private CompletableFuture<AIResponseDTO> llamarPreset(String presetId, PromptRequestDTO request, String apiKey) {

		String body = construirBody(presetId, request);
		Map<String, String> headers = construirHeaders(apiKey);
		long inicio = System.currentTimeMillis();

		return externalHandler.doPostAsync(openRouterUrl, headers, body)
				.thenApply(response -> parsearRespuesta(response, presetId, inicio)).exceptionally(ex -> {
					LOGGER.log(Level.WARNING, "Error al llamar el preset {0}: {1}",
							new Object[] { presetId, ex.getMessage() });
					return respuestaFallida(presetId, ex.getMessage(), inicio);
				});
	}

	/**
	 * Arma el JSON del body que se envía a OpenRouter.
	 *
	 * <p>
	 * El formato sigue el estándar Chat Completions de OpenAI que OpenRouter
	 * implementa. El texto del usuario y el formato deseado se concatenan en el
	 * campo {@code content} del mensaje con rol {@code user}.
	 * </p>
	 *
	 * @param presetId ID del preset de OpenRouter
	 * @param request  request del usuario con el prompt y el formato
	 * @return cadena JSON lista para enviar como body
	 */
	private String construirBody(String presetId, PromptRequestDTO request) {
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
	 * Construye los headers HTTP necesarios para autenticarse con OpenRouter.
	 *
	 * @param apiKey API key de OpenRouter en texto plano
	 * @return mapa de cabeceras HTTP
	 */
	private Map<String, String> construirHeaders(String apiKey) {
		return Map.of("Authorization", "Bearer " + apiKey, "Content-Type", "application/json", "HTTP-Referer",
				"https://conclave.unbosque.edu.co", "X-Title", "Conclave");
	}

	/**
	 * Parsea la respuesta HTTP de OpenRouter y la convierte en un
	 * {@link AIResponseDTO}.
	 *
	 * <p>
	 * Lee {@code choices[0].message.content} como resultado principal. Si la
	 * respuesta no es 2xx o el JSON no tiene el campo esperado, retorna un DTO con
	 * {@link ResponseStatus#FAILED}.
	 * </p>
	 *
	 * @param response respuesta HTTP recibida de OpenRouter
	 * @param presetId ID del preset que generó la respuesta
	 * @param inicio   marca de tiempo en ms antes de lanzar la petición
	 * @return DTO con la respuesta normalizada
	 */
	private AIResponseDTO parsearRespuesta(HttpResponse<String> response, String presetId, long inicio) {

		long duracion = System.currentTimeMillis() - inicio;
		AIResponseDTO dto = new AIResponseDTO();
		dto.setProvider(AIProvider.OPENROUTER);
		dto.setModel(presetId);
		dto.setHttpStatus(response.statusCode());
		dto.setDurationMs(duracion);

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			dto.setStatus(ResponseStatus.FAILED);
			dto.setErrorMessage(
					"OpenRouter respondió con HTTP " + response.statusCode() + " para el preset " + presetId);
			return dto;
		}

		try {
			JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
			JsonArray choices = json.getAsJsonArray("choices");

			if (choices == null || choices.isEmpty()) {
				dto.setStatus(ResponseStatus.FAILED);
				dto.setErrorMessage("La respuesta de " + presetId + " no contiene choices.");
				return dto;
			}

			JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
			String content = message.has("content") && !message.get("content").isJsonNull()
					? message.get("content").getAsString()
					: "";

			dto.setDownloadUrl(content);
			dto.setStatus(ResponseStatus.SUCCESS);

		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, "Error al parsear la respuesta del preset {0}: {1}",
					new Object[] { presetId, ex.getMessage() });
			dto.setStatus(ResponseStatus.FAILED);
			dto.setErrorMessage("Error al parsear la respuesta: " + ex.getMessage());
		}

		return dto;
	}

	/**
	 * Construye un {@link AIResponseDTO} de error para cuando la promesa del preset
	 * lanza una excepción.
	 *
	 * @param presetId     ID del preset que falló
	 * @param errorMessage mensaje de la excepción
	 * @param inicio       marca de tiempo en ms antes de lanzar la petición
	 * @return DTO con estado {@link ResponseStatus#FAILED}
	 */
	private AIResponseDTO respuestaFallida(String presetId, String errorMessage, long inicio) {
		AIResponseDTO dto = new AIResponseDTO();
		dto.setProvider(AIProvider.OPENROUTER);
		dto.setModel(presetId);
		dto.setStatus(ResponseStatus.FAILED);
		dto.setHttpStatus(500);
		dto.setErrorMessage(errorMessage);
		dto.setDurationMs(System.currentTimeMillis() - inicio);
		return dto;
	}
}