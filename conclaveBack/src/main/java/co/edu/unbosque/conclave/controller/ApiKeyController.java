package co.edu.unbosque.conclave.controller;

import co.edu.unbosque.conclave.dto.ApiKeyDTO;
import co.edu.unbosque.conclave.dto.ApiKeyUpdateDTO;
import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de API keys de los proveedores de IA.
 * Todos los endpoints requieren rol <strong>ADMIN</strong>.
 *
 * <p>Las claves nunca se devuelven en texto plano; las respuestas muestran
 * siempre una versión enmascarada.</p>
 */
@RestController
@RequestMapping("/admin/apikey")
@CrossOrigin(origins = {"http://localhost:8080"})
@Transactional
@Tag(name = "Gestión de API Keys",
		description = "Endpoints para administrar las API keys de los proveedores de IA")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

	/** Servicio para operaciones con las API keys. */
	@Autowired
	private ApiKeyService apiKeyService;

	/** Constructor por defecto. */
	public ApiKeyController() {
	}
	
	/**
	 * Devuelve todas las API keys con la clave enmascarada. Requiere rol ADMIN.
	 *
	 * @return lista de {@link ApiKeyDTO} con información de cada proveedor
	 */
	@Operation(
			summary = "Obtener todas las API keys",
			description = """
					Devuelve la lista de API keys configuradas para cada proveedor de IA.
					Las claves aparecen enmascaradas (`****XXXX`).

					Requiere rol **ADMIN**.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista de API keys",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = ApiKeyDTO.class))),
			@ApiResponse(responseCode = "204", description = "Sin API keys registradas")
	})
	@GetMapping("/getall")
	ResponseEntity<List<ApiKeyDTO>> getAll() {
		List<ApiKeyDTO> keys = apiKeyService.getAll();
		if (keys.isEmpty()) {
			return new ResponseEntity<>(keys, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(keys, HttpStatus.ACCEPTED);
	}

	/**
	 * Actualiza la clave y/o el estado activo de un proveedor. Requiere rol ADMIN.
	 *
	 * @param provider  proveedor cuya key se actualiza
	 * @param data      DTO con la nueva clave en texto plano y/o el nuevo estado activo
	 * @param auth      autenticación del administrador (para registrar quién actualizó)
	 * @return mensaje de éxito o error
	 */
	@Operation(
			summary = "Actualizar API key de un proveedor",
			description = """
					Actualiza la clave y/o el estado activo del proveedor indicado. Para CLOUDFLARE, `newKey` debe contener `ACCOUNT_ID|API_TOKEN`. Para los modelos HF_*, `newKey` es el token de Hugging Face. Para los demás proveedores, `newKey` es el token o API key de cada servicio.

					- Si `newKey` está presente, se cifra con AES antes de persistirla.
					- Si `active` está presente, se cambia el estado de la clave.
					- Ambos campos son opcionales; se puede enviar solo uno.

					Requiere rol **ADMIN**.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "API key actualizada exitosamente",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "API key actualizada exitosamente"))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Proveedor no encontrado")))
	})
	@PutMapping(path = "/update/{provider}", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> update(
			@Parameter(description = "Proveedor de IA", required = true,
					example = "CLOUDFLARE")
			@PathVariable AIProvider provider,
			@Parameter(description = "Nueva clave y/o estado",
					required = true,
					schema = @Schema(implementation = ApiKeyUpdateDTO.class),
					examples = @ExampleObject(
							value = "{\"newKey\":\"ACCOUNT_ID|CLOUDFLARE_API_TOKEN\",\"active\":true}"))
			@RequestBody ApiKeyUpdateDTO data,
			Authentication auth) {

		int status = apiKeyService.updateKey(provider, data, auth.getName());
		if (status == 0) {
			return new ResponseEntity<>("API key actualizada exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Proveedor no encontrado", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Activa o desactiva la API key de un proveedor sin cambiar la clave. Requiere rol ADMIN.
	 *
	 * @param provider proveedor a activar o desactivar
	 * @param active   {@code true} para activar, {@code false} para desactivar
	 * @return mensaje de éxito o error
	 */
	@Operation(
			summary = "Activar / desactivar API key",
			description = """
					Cambia el estado activo de la API key de un proveedor sin modificar la clave.

					Requiere rol **ADMIN**.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Estado actualizado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Estado actualizado exitosamente"))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Proveedor no encontrado")))
	})
	@PutMapping("/setenabled/{provider}")
	ResponseEntity<String> setActive(
			@Parameter(description = "Proveedor de IA", required = true,
					example = "CLOUDFLARE")
			@PathVariable AIProvider provider,
			@Parameter(description = "true = activar, false = desactivar",
					required = true, example = "true")
			@RequestParam boolean active) {

		int status = apiKeyService.setActive(provider, active);
		if (status == 0) {
			return new ResponseEntity<>("Estado actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Proveedor no encontrado", HttpStatus.NOT_FOUND);
		}
	}

}