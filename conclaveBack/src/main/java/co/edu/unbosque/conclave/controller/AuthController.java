package co.edu.unbosque.conclave.controller;

import co.edu.unbosque.conclave.dto.AuthResponseDTO;
import co.edu.unbosque.conclave.dto.UserDTO;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.security.JwtUtil;
import co.edu.unbosque.conclave.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Controlador REST para la autenticación de usuarios.
 * Maneja las operaciones de inicio de sesión y registro.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "API para autenticación de usuarios (login y registro)")
public class AuthController {

	/** Logger de la clase para registrar errores de autenticación. */
	private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

	/** Gestor de autenticación para validar credenciales de usuario. */
	private final AuthenticationManager authenticationManager;

	/** Utilidad para operaciones con tokens JWT. */
	private final JwtUtil jwtUtil;

	/** Servicio para operaciones relacionadas con usuarios. */
	private final UserService userService;

	/**
	 * Constructor que inicializa las dependencias del controlador.
	 *
	 * @param authenticationManager gestor de autenticación
	 * @param jwtUtil               utilidad para tokens JWT
	 * @param userService           servicio de usuarios
	 */
	public AuthController(AuthenticationManager authenticationManager,
			JwtUtil jwtUtil, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	/**
	 * Autentica al usuario y genera un token JWT si las credenciales son válidas.
	 *
	 * @param loginRequest DTO con las credenciales de inicio de sesión
	 * @return {@link AuthResponseDTO} con el token, username y rol, o 401 si falla
	 */
	@Operation(
			summary = "Iniciar sesión",
			description = """
					Verifica las credenciales del usuario y genera un token JWT:
					1. Envía username y password en formato JSON.
					2. Si son correctas, recibirás un token JWT.
					3. Incluye el token en el encabezado: `Authorization: Bearer <token>`.

					El token es válido durante 24 horas.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = AuthResponseDTO.class),
							examples = @ExampleObject(
									value = "{\"token\":\"eyJ...\",\"username\":\"admin\",\"role\":\"ADMIN\"}"))),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(
									value = "Nombre de usuario o contraseña inválidos")))
	})
	@PostMapping("/login")
	public ResponseEntity<?> login(
			@Parameter(description = "Credenciales del usuario", required = true,
					schema = @Schema(implementation = UserDTO.class),
					examples = @ExampleObject(
							value = "{\"username\":\"admin\",\"password\":\"1234567890\"}"))
			@RequestBody UserDTO loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequest.getUsername(), loginRequest.getPassword()));

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String jwt = jwtUtil.generateToken(userDetails);

			String role = null;
			if (userDetails instanceof User user) {
				role = user.getRole().name();
			}

			return ResponseEntity.ok(
					new AuthResponseDTO(jwt, userDetails.getUsername(), role));
		} catch (AuthenticationException e) {
			LOGGER.log(Level.WARNING, "Intento de autenticación fallido para: {0}",
					loginRequest.getUsername());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Nombre de usuario o contraseña inválidos o usuario no encontrado");
		}
	}

	/**
	 * Registra un nuevo usuario.
	 * El email es obligatorio; el rol siempre se fuerza a {@code USER}.
	 *
	 * @param registerRequest DTO con los datos del nuevo usuario
	 * @return 201 si el registro es exitoso, 409 si el username ya existe,
	 *         406 si el email ya existe, 400 si la contraseña es inválida
	 */
	@Operation(
			summary = "Registrar usuario",
			description = """
					Crea una nueva cuenta de usuario.

					**Requisitos:**
					- `username`: único, obligatorio.
					- `email`: único, obligatorio.
					- `password`: mínimo 8 caracteres, una minúscula, una mayúscula, un número y un símbolo.

					El rol siempre se establece como **USER**, independientemente de lo que se envíe.
					""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Usuario registrado exitosamente"))),
			@ApiResponse(responseCode = "409", description = "El nombre de usuario ya existe",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "El nombre de usuario ya existe"))),
			@ApiResponse(responseCode = "406", description = "El email ya está registrado",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "El correo electrónico ya está en uso"))),
			@ApiResponse(responseCode = "400", description = "Contraseña no válida",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "Error al registrar el usuario")))
	})
	@PostMapping("/register")
	public ResponseEntity<?> register(
			@Parameter(description = "Datos del nuevo usuario", required = true,
					schema = @Schema(implementation = UserDTO.class),
					examples = @ExampleObject(
							value = "{\"username\":\"nuevoUsuario\",\"email\":\"user@example.com\","
									+ "\"password\":\"Contra1@segura\"}"))
			@RequestBody UserDTO registerRequest) {

		// El rol siempre es USER, sin importar lo que envíe el cliente
		registerRequest.setRole(User.Role.USER);

		int result = userService.create(registerRequest);
		return switch (result) {
			case 0 -> ResponseEntity.status(HttpStatus.CREATED)
					.body("Usuario registrado exitosamente");
			case 1 -> ResponseEntity.status(HttpStatus.CONFLICT)
					.body("El nombre de usuario ya existe");
			case 2 -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
					.body("El correo electrónico ya está en uso");
			default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Error al registrar el usuario");
		};
	}

}
