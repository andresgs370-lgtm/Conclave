package co.edu.unbosque.conclave.controller;

import co.edu.unbosque.conclave.dto.AIResponseDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;
import co.edu.unbosque.conclave.exception.ApiKeyNotConfiguredException;
import co.edu.unbosque.conclave.exception.UnsupportedOutputTypeException;
import co.edu.unbosque.conclave.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la generación de contenido multimedia mediante
 * OpenRouter.
 *
 * <p>
 * Expone el endpoint {@code POST /prompt/generar} que recibe el prompt del
 * usuario y el tipo de salida deseado, lanza en paralelo las peticiones a los
 * presets de OpenRouter y devuelve la lista de respuestas de cada modelo.
 * </p>
 *
 * <p>
 * Este controlador <strong>no</strong> interactúa con la base de datos. Los
 * resultados viajan directamente desde OpenRouter al frontend.
 * </p>
 *
 * <p>
 * Requiere autenticación JWT con rol {@code USER} o {@code ADMIN}.
 * </p>
 */
@RestController
@RequestMapping("/prompt")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:4200" })
@Tag(name = "Generación de contenido", description = "Envía un prompt a OpenRouter y retorna las respuestas de los modelos en paralelo")
@SecurityRequirement(name = "bearerAuth")
public class PromptController {

	/** Servicio que orquesta las llamadas asíncronas a OpenRouter. */
	@Autowired
	private PromptService promptService;

	/** Constructor por defecto. */
	public PromptController() {
	}

	/**
	 * Recibe el prompt del usuario, selecciona los presets de OpenRouter según el
	 * tipo de salida y retorna las respuestas de todos los modelos en paralelo.
	 *
	 * <p>
	 * Los posibles valores de {@code outputType} son:
	 * </p>
	 * <ul>
	 * <li>{@code IMAGE} — 4 modelos en paralelo</li>
	 * <li>{@code AUDIO} — 2 modelos en paralelo</li>
	 * <li>{@code VIDEO} — 1 modelo</li>
	 * </ul>
	 *
	 * @param request DTO con el texto del prompt, el tipo de salida y el formato
	 * @return lista de {@link AIResponseDTO} con la respuesta de cada preset, o
	 *         {@code 503} si la API key no está configurada, o {@code 400} si el
	 *         tipo de salida no tiene presets asignados, o {@code 500} si ocurre un
	 *         error inesperado
	 */
	@Operation(summary = "Generar contenido multimedia con OpenRouter", description = """
			Envía el prompt a los presets de OpenRouter según el tipo de salida elegido
			y retorna la respuesta de cada modelo.

			- `IMAGE` dispara 4 modelos en paralelo.
			- `AUDIO` dispara 2 modelos en paralelo.
			- `VIDEO` dispara 1 modelo.

			Cada elemento de la lista de respuesta identifica el modelo que lo generó
			y el contenido (URL o base64) listo para mostrar o descargar.

			Requiere rol **USER** o **ADMIN**.
			""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Respuestas de los modelos generadas exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AIResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Tipo de salida no soportado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Solicitud inválida\","
					+ "\"message\":\"El proveedor OpenRouter no soporta el tipo de salida: TEXT\"}"))),
			@ApiResponse(responseCode = "503", description = "API key de OpenRouter no configurada", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"status\":503,\"error\":\"Servicio no disponible\","
					+ "\"message\":\"La API key del proveedor OpenRouter no está configurada.\"}"))),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Error interno del servidor\","
					+ "\"message\":\"Error inesperado al procesar la solicitud\"}"))) })
	@PostMapping(path = "/generar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AIResponseDTO>> generar(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Prompt del usuario con el tipo de salida y formato deseado", required = true, content = @Content(schema = @Schema(implementation = PromptRequestDTO.class), examples = @ExampleObject(value = "{\"promptText\":\"Un atardecer en las montañas\","
					+ "\"outputType\":\"IMAGE\",\"format\":\"PNG\"}"))) @Valid @RequestBody PromptRequestDTO request)
			throws ApiKeyNotConfiguredException, UnsupportedOutputTypeException {

		List<AIResponseDTO> respuestas = promptService.generar(request);
		return new ResponseEntity<>(respuestas, HttpStatus.OK);
	}
}
