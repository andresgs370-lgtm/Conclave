package co.edu.unbosque.conclave.controller;

import co.edu.unbosque.conclave.dto.UserDTO;
import co.edu.unbosque.conclave.model.User.Role;
import co.edu.unbosque.conclave.service.UserService;
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
 * Controlador REST para la gestión de usuarios.
 * Proporciona endpoints para crear, leer, actualizar y eliminar usuarios.
 * Requiere autenticación JWT para todos los endpoints.
 *
 * <ul>
 *   <li>{@code /user/me}, {@code /user/getall}, {@code /user/count},
 *       {@code /user/exists/*}, {@code /user/getbyid/*}: accesibles para USER o ADMIN.</li>
 *   <li>Todos los demás: solo ADMIN.</li>
 * </ul>
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8081", "http://localhost:4200"})
@Transactional
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administrar usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	/** Servicio para operaciones relacionadas con usuarios. */
	@Autowired
	private UserService userServ;

	/** Constructor por defecto. */
	public UserController(){
		
	}
	
	/**
	 * Devuelve los datos del usuario autenticado actualmente.
	 *
	 * @param auth objeto de autenticación del contexto de seguridad
	 * @return DTO del usuario autenticado o 404 si no se encuentra
	 */
	@Operation(summary = "Obtener perfil propio",
			description = "Devuelve los datos del usuario que está autenticado. "
					+ "Accesible para USER y ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Usuario encontrado",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{}")))
	})
	@GetMapping("/me")
	ResponseEntity<UserDTO> getMe(Authentication auth) {
		UserDTO found = userServ.getByUsername(auth.getName());
		if (found != null) {
			return new ResponseEntity<>(found, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(new UserDTO(), HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * Crea un nuevo usuario utilizando datos en formato JSON. Requiere rol ADMIN.
	 *
	 * @param newUser DTO con los datos del usuario a crear
	 * @return mensaje de éxito o error
	 */
	@Operation(summary = "Crear usuario (JSON)",
			description = """
					Crea un nuevo usuario enviando datos en formato JSON.

					**Posibles resultados:**
					- `201`: usuario creado.
					- `406`: username ya existe.
					- `409`: email ya existe.
					- `400`: contraseña inválida o caracteres no permitidos.

					Requiere rol **ADMIN**.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario creado exitosamente"))),
			@ApiResponse(responseCode = "406", description = "Username ya en uso",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(
									value = "Error al crear el usuario, el nombre de usuario ya está en uso"))),
			@ApiResponse(responseCode = "409", description = "Email ya registrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "El correo electrónico ya está en uso"))),
			@ApiResponse(responseCode = "400", description = "Solicitud inválida",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Solicitud con caracteres inválidos")))
	})
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> createWithJSON(
			@Parameter(description = "Datos del nuevo usuario", required = true,
					schema = @Schema(implementation = UserDTO.class),
					examples = @ExampleObject(
							value = "{\"username\":\"nuevoUsuario\","
									+ "\"email\":\"user@example.com\","
									+ "\"password\":\"Contra1@segura\","
									+ "\"role\":\"USER\"}"))
			@RequestBody UserDTO newUser) {

		if (newUser.getUsername() != null
				&& (newUser.getUsername().contains("<") || newUser.getUsername().contains(">"))) {
			return new ResponseEntity<>("Solicitud con caracteres inválidos", HttpStatus.BAD_REQUEST);
		}

		int status = userServ.create(newUser);
		return switch (status) {
			case 0 -> new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
			case 1 -> new ResponseEntity<>(
					"Error al crear el usuario, el nombre de usuario ya está en uso",
					HttpStatus.NOT_ACCEPTABLE);
			case 2 -> new ResponseEntity<>("El correo electrónico ya está en uso",
					HttpStatus.CONFLICT);
			default -> new ResponseEntity<>("Contraseña no válida", HttpStatus.BAD_REQUEST);
		};
	}

	/**
	 * Obtiene todos los usuarios. Accesible para USER y ADMIN.
	 *
	 * @return lista de DTOs de usuarios
	 */
	@Operation(summary = "Obtener todos los usuarios",
			description = "Recupera todos los usuarios registrados. Accesible para USER y ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista de usuarios",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "204", description = "Sin usuarios registrados")
	})
	@GetMapping("/getall")
	ResponseEntity<List<UserDTO>> getAll() {
		List<UserDTO> users = userServ.getAll();
		if (users.isEmpty()) {
			return new ResponseEntity<>(users, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(users, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Cuenta el número total de usuarios. Accesible para USER y ADMIN.
	 *
	 * @return número total de usuarios
	 */
	@Operation(summary = "Contar usuarios",
			description = "Devuelve el número total de usuarios registrados.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Conteo exitoso",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "5"))),
			@ApiResponse(responseCode = "204", description = "Sin usuarios")
	})
	@GetMapping("/count")
	ResponseEntity<Long> count() {
		Long count = userServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un usuario con el ID especificado. Accesible para USER y ADMIN.
	 *
	 * @param id ID del usuario a verificar
	 * @return {@code true} si existe, {@code false} si no
	 */
	@Operation(summary = "Verificar existencia de usuario",
			description = "Comprueba si existe un usuario con el ID indicado.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Usuario encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "true"))),
			@ApiResponse(responseCode = "204", description = "No encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "false")))
	})
	@GetMapping("/exists/{id}")
	ResponseEntity<Boolean> exists(
			@Parameter(description = "ID del usuario", required = true, example = "1")
			@PathVariable Long id) {
		boolean found = userServ.exist(id);
		if (found) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Obtiene un usuario por su ID. Accesible para USER y ADMIN.
	 *
	 * @param id ID del usuario a buscar
	 * @return DTO del usuario o 404
	 */
	@Operation(summary = "Obtener usuario por ID",
			description = "Busca un usuario por su ID y devuelve sus datos.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Usuario encontrado",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "404", description = "No encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{}")))
	})
	@GetMapping("/getbyid/{id}")
	ResponseEntity<UserDTO> getById(
			@Parameter(description = "ID del usuario", required = true, example = "1")
			@PathVariable Long id) {
		UserDTO found = userServ.getById(id);
		if (found != null) {
			return new ResponseEntity<>(found, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(new UserDTO(), HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Actualiza un usuario existente utilizando datos JSON. Requiere rol ADMIN.
	 *
	 * @param id      ID del usuario a actualizar
	 * @param newUser DTO con los nuevos datos
	 * @return mensaje de éxito o error
	 */
	@Operation(summary = "Actualizar usuario (JSON)",
			description = """
					Actualiza los datos de un usuario existente enviando los nuevos datos en JSON.

					**Posibles resultados:**
					- `202`: actualizado.
					- `226`: el nuevo username ya está en uso.
					- `404`: usuario no encontrado.
					- `400`: error al actualizar.

					Requiere rol **ADMIN**.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Actualizado exitosamente",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario actualizado exitosamente"))),
			@ApiResponse(responseCode = "226", description = "Username ya en uso",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "El nuevo nombre de usuario ya está en uso"))),
			@ApiResponse(responseCode = "404", description = "No encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario no encontrado"))),
			@ApiResponse(responseCode = "400", description = "Error",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Error al actualizar")))
	})
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> updateWithJSON(
			@Parameter(description = "ID del usuario", required = true, example = "1")
			@RequestParam Long id,
			@Parameter(description = "Nuevos datos del usuario", required = true,
					schema = @Schema(implementation = UserDTO.class))
			@RequestBody UserDTO newUser) {

		int status = userServ.updateById(id, newUser);
		return switch (status) {
			case 0 -> new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
			case 1 -> new ResponseEntity<>("El nuevo nombre de usuario ya está en uso",
					HttpStatus.IM_USED);
			case 2 -> new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			default -> new ResponseEntity<>("Error al actualizar", HttpStatus.BAD_REQUEST);
		};
	}

	/**
	 * Habilita o deshabilita una cuenta de usuario. Requiere rol ADMIN.
	 *
	 * @param id      ID del usuario
	 * @param enabled {@code true} para habilitar, {@code false} para deshabilitar
	 * @return mensaje de éxito o error
	 */
	@Operation(summary = "Cambiar estado de cuenta",
			description = "Habilita o deshabilita la cuenta de un usuario. Requiere rol **ADMIN**.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Estado actualizado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Estado actualizado exitosamente"))),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario no encontrado")))
	})
	@PutMapping("/setenabled/{id}")
	ResponseEntity<String> setEnabled(
			@Parameter(description = "ID del usuario", required = true, example = "1")
			@PathVariable Long id,
			@Parameter(description = "true = habilitar, false = deshabilitar",
					required = true, example = "false")
			@RequestParam boolean enabled) {

		int status = userServ.setEnabled(id, enabled);
		if (status == 0) {
			return new ResponseEntity<>("Estado actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Elimina un usuario por su ID. Requiere rol ADMIN.
	 *
	 * @param id ID del usuario a eliminar
	 * @return mensaje de éxito o error
	 */
	@Operation(summary = "Eliminar usuario por ID",
			description = "Elimina permanentemente un usuario. Requiere rol **ADMIN**.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Eliminado exitosamente",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario eliminado exitosamente"))),
			@ApiResponse(responseCode = "404", description = "No encontrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Error al eliminar")))
	})
	@DeleteMapping("/deletebyid/{id}")
	ResponseEntity<String> deleteById(
			@Parameter(description = "ID del usuario", required = true, example = "1")
			@PathVariable Long id) {
		int status = userServ.deleteById(id);
		if (status == 0) {
			return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}

}
