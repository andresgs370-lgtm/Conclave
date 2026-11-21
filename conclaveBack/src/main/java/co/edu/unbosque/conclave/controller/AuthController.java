package co.edu.unbosque.conclave.controller;

import co.edu.unbosque.conclave.dto.AuthResponseDTO;
import co.edu.unbosque.conclave.dto.ResetPasswordDTO;
import co.edu.unbosque.conclave.dto.UserDTO;
import co.edu.unbosque.conclave.dto.VerificacionDTO;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.security.JwtUtil;
import co.edu.unbosque.conclave.service.EmailAuthService;
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

	/** Servicio para verificacion de correo y reset de contrasena. */
	private final EmailAuthService emailAuthService;

	/**
	 * Constructor que inicializa las dependencias del controlador.
	 *
	 * @param authenticationManager gestor de autenticación
	 * @param jwtUtil               utilidad para tokens JWT
	 * @param userService           servicio de usuarios
	 * @param emailAuthService      servicio de verificacion por correo
	 */
	public AuthController(AuthenticationManager authenticationManager,
			JwtUtil jwtUtil, UserService userService,
			EmailAuthService emailAuthService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
		this.emailAuthService = emailAuthService;
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
		if (loginRequest == null || loginRequest.getUsername() == null
				|| loginRequest.getPassword() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Credenciales incompletas");
		}
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequest.getUsername().trim(), loginRequest.getPassword()));

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
					loginRequest.getUsername().trim());
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
		if (registerRequest == null || registerRequest.getEmail() == null
				|| registerRequest.getEmail().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Datos de registro incompletos");
		}

		// El rol siempre es USER, sin importar lo que envíe el cliente
		registerRequest.setRole(User.Role.USER);

		int result = userService.create(registerRequest);
		if (result != 0) {
			return switch (result) {
				case 1 -> ResponseEntity.status(HttpStatus.CONFLICT)
						.body("El nombre de usuario ya existe");
				case 2 -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
						.body("El correo electronico ya esta en uso");
				default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Error al registrar el usuario");
			};
		}

		String normalizedEmail = registerRequest.getEmail().trim().toLowerCase();
		int envio = emailAuthService.enviarCodigoRegistro(normalizedEmail);
		if (envio == 2) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Usuario creado pero no se pudo enviar el correo de verificacion.");
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body("Usuario registrado. Revisa tu correo para verificar tu cuenta.");
	}

	/**
	 * Reenvía el codigo de verificacion al correo del usuario.
	 * Util cuando el codigo anterior expiro o no llego.
	 *
	 * @param dto DTO con el email del usuario
	 * @return 200 si el correo fue enviado, 404 si el usuario no existe
	 */
	@Operation(
			summary = "Reenviar codigo de verificacion",
			description = "Genera un nuevo codigo y lo envia al correo del usuario. "
					+ "Usar cuando el codigo anterior expiro.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Codigo reenviado al correo"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error al enviar el correo")
	})
	@PostMapping("/reenviar-codigo")
	public ResponseEntity<?> reenviarCodigo(
			@RequestBody VerificacionDTO dto) {

		int result = emailAuthService.enviarCodigoRegistro(dto.getEmail());
		return switch (result) {
			case 0 -> ResponseEntity.ok("Codigo reenviado. Revisa tu correo.");
			case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No existe una cuenta con ese correo.");
			default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("No se pudo enviar el correo. Intenta de nuevo.");
		};
	}

	/**
	 * Verifica el codigo de registro enviado al correo del usuario.
	 * Si es correcto y no ha expirado, activa la cuenta.
	 *
	 * @param dto DTO con email y codigo
	 * @return 200 si la cuenta fue activada, 400/404/410 segun el error
	 */
	@Operation(
			summary = "Verificar cuenta por correo",
			description = "Valida el codigo de 6 digitos enviado al correo al registrarse. "
					+ "Si es correcto activa la cuenta.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Cuenta verificada exitosamente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "400", description = "Codigo incorrecto"),
			@ApiResponse(responseCode = "410", description = "Codigo expirado")
	})
	@PostMapping("/verificar-registro")
	public ResponseEntity<?> verificarRegistro(
			@RequestBody VerificacionDTO dto) {

		int result = emailAuthService.verificarRegistro(dto.getEmail(), dto.getCodigo());
		return switch (result) {
			case 0 -> ResponseEntity.ok("Cuenta verificada. Ya puedes iniciar sesion.");
			case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Usuario no encontrado.");
			case 2 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Codigo incorrecto.");
			default -> ResponseEntity.status(HttpStatus.GONE)
					.body("El codigo ha expirado. Solicita uno nuevo.");
		};
	}

	/**
	 * Solicita el envio de un codigo de restablecimiento de contrasena al correo.
	 *
	 * @param dto DTO con el email del usuario
	 * @return 200 si el correo fue enviado, 404 si el usuario no existe
	 */
	@Operation(
			summary = "Solicitar reset de contrasena",
			description = "Envia un codigo de 6 digitos al correo del usuario "
					+ "para restablecer su contrasena.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Codigo enviado al correo"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error al enviar el correo")
	})
	@PostMapping("/solicitar-reset")
	public ResponseEntity<?> solicitarReset(
			@RequestBody VerificacionDTO dto) {

		int result = emailAuthService.solicitarReset(dto.getEmail());
		return switch (result) {
			case 0 -> ResponseEntity.ok("Codigo de restablecimiento enviado a tu correo.");
			case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No existe una cuenta con ese correo.");
			default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("No se pudo enviar el correo. Intenta de nuevo.");
		};
	}

	/**
	 * Confirma el restablecimiento de contrasena con el codigo recibido por correo.
	 *
	 * @param dto DTO con email, codigo y nueva contrasena
	 * @return 200 si la contrasena fue actualizada, error segun el caso
	 */
	@Operation(
			summary = "Confirmar reset de contrasena",
			description = "Valida el codigo y actualiza la contrasena del usuario. "
					+ "Requiere email, codigo y nuevaPassword.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Contrasena actualizada correctamente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "400", description = "Codigo incorrecto"),
			@ApiResponse(responseCode = "410", description = "Codigo expirado")
	})
	@PostMapping("/confirmar-reset")
	public ResponseEntity<?> confirmarReset(
			@RequestBody ResetPasswordDTO dto) {

		int result = emailAuthService.confirmarReset(
				dto.getEmail(), dto.getCodigo(), dto.getNuevaPassword());
		return switch (result) {
		case 0 -> ResponseEntity.ok("Contrasena actualizada correctamente.");
		case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("Usuario no encontrado.");
		case 2 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("Codigo incorrecto.");
		case 4 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("La nueva contrasena no cumple los requisitos de seguridad.");
		default -> ResponseEntity.status(HttpStatus.GONE)
				.body("El codigo ha expirado. Solicita uno nuevo.");
	};
	}

}
