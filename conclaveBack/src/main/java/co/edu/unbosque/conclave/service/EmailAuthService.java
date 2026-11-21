package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Servicio que maneja la verificacion de correo al registro y el
 * restablecimiento de contrasena mediante codigo enviado por correo.
 *
 * <p>Genera codigos aleatorios de 6 digitos, los guarda en el usuario
 * con una expiracion de 10 minutos y los valida cuando el usuario
 * los ingresa desde el frontend.</p>
 *
 * <p>No toca JWT ni la logica de login. Solo maneja codigos temporales.</p>
 */
@Service
public class EmailAuthService {

	/** Logger de la clase. */
	private static final Logger LOGGER = Logger.getLogger(EmailAuthService.class.getName());

	/** Minutos de validez del codigo de verificacion. */
	private static final int EXPIRY_MINUTES = 10;

	/** Generador criptograficamente seguro para codigos temporales. */
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/** Patron de validacion de contrasena. */
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(
			"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");

	/** Repositorio de usuarios. */
	@Autowired
	private UserRepository userRepo;

	/** Servicio de envio de correos. */
	@Autowired
	private EmailService emailService;

	/** Codificador de contrasenas. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/** Constructor por defecto. */
	public EmailAuthService() {
	}

	/**
	 * Genera un codigo de verificacion, lo guarda en el usuario con expiracion
	 * de 10 minutos y lo envia al correo registrado.
	 *
	 * <p>Resultados posibles:</p>
	 * <ul>
	 *   <li>{@code 0} — codigo enviado correctamente.</li>
	 *   <li>{@code 1} — usuario no encontrado con ese correo.</li>
	 *   <li>{@code 2} — fallo el envio del correo.</li>
	 * </ul>
	 *
	 * @param email correo del usuario recien registrado
	 * @return codigo de resultado
	 */
	public int enviarCodigoRegistro(String email) {
		if (email == null || email.isBlank()) {
			return 1;
		}
		String normalizedEmail = email.trim().toLowerCase();
		Optional<User> found = userRepo.findByEmail(normalizedEmail);
		if (found.isEmpty()) {
			return 1;
		}

		User user = found.get();
		String codigo = generarCodigo();
		user.setVerificationCode(codigo);
		user.setVerificationExpiry(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
		userRepo.save(user);

		boolean enviado = emailService.enviarCodigoVerificacion(normalizedEmail, codigo);
		if (!enviado) {
			LOGGER.log(Level.SEVERE, "No se pudo enviar el correo de verificacion a {0}", normalizedEmail);
			return 2;
		}
		return 0;
	}

	/**
	 * Verifica el codigo de registro ingresado por el usuario.
	 * Si es correcto y no ha expirado, activa la cuenta.
	 *
	 * <p>Resultados posibles:</p>
	 * <ul>
	 *   <li>{@code 0} — cuenta verificada y activada.</li>
	 *   <li>{@code 1} — usuario no encontrado.</li>
	 *   <li>{@code 2} — codigo incorrecto.</li>
	 *   <li>{@code 3} — codigo expirado.</li>
	 * </ul>
	 *
	 * @param email  correo del usuario
	 * @param codigo codigo ingresado por el usuario
	 * @return codigo de resultado
	 */
	public int verificarRegistro(String email, String codigo) {
		if (email == null || codigo == null || email.isBlank() || codigo.isBlank()) {
			return 2;
		}
		String normalizedEmail = email.trim().toLowerCase();
		Optional<User> found = userRepo.findByEmail(normalizedEmail);
		if (found.isEmpty()) {
			return 1;
		}

		User user = found.get();
		if (user.isEnabled()) {
			return 0;
		}

		if (user.getVerificationCode() == null || !codigo.trim().equals(user.getVerificationCode())) {
			return 2;
		}

		if (user.getVerificationExpiry() == null
				|| LocalDateTime.now().isAfter(user.getVerificationExpiry())) {
			return 3;
		}

		user.setEnabled(true);
		user.setVerificationCode(null);
		user.setVerificationExpiry(null);
		userRepo.save(user);
		return 0;
	}

	/**
	 * Genera un codigo de reset, lo guarda en el usuario con expiracion
	 * de 10 minutos y lo envia al correo registrado.
	 *
	 * <p>Resultados posibles:</p>
	 * <ul>
	 *   <li>{@code 0} — codigo enviado correctamente.</li>
	 *   <li>{@code 1} — usuario no encontrado con ese correo.</li>
	 *   <li>{@code 2} — fallo el envio del correo.</li>
	 * </ul>
	 *
	 * @param email correo del usuario que solicita el reset
	 * @return codigo de resultado
	 */
	public int solicitarReset(String email) {
		if (email == null || email.isBlank()) {
			return 1;
		}
		String normalizedEmail = email.trim().toLowerCase();
		Optional<User> found = userRepo.findByEmail(normalizedEmail);
		if (found.isEmpty()) {
			return 1;
		}

		User user = found.get();
		String codigo = generarCodigo();
		user.setVerificationCode(codigo);
		user.setVerificationExpiry(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
		userRepo.save(user);

		boolean enviado = emailService.enviarCodigoReset(normalizedEmail, codigo);
		if (!enviado) {
			LOGGER.log(Level.SEVERE, "No se pudo enviar el correo de reset a {0}", normalizedEmail);
			return 2;
		}
		return 0;
	}

	/**
	 * Confirma el restablecimiento de contrasena validando el correo, el codigo
	 * temporal y la complejidad de la nueva contrasena.
	 *
	 * <p>Resultados posibles:</p>
	 * <ul>
	 *   <li>{@code 0} — contrasena actualizada correctamente.</li>
	 *   <li>{@code 1} — usuario no encontrado.</li>
	 *   <li>{@code 2} — codigo incorrecto o vacio.</li>
	 *   <li>{@code 3} — codigo expirado.</li>
	 *   <li>{@code 4} — la nueva contrasena no cumple los requisitos.</li>
	 * </ul>
	 *
	 * @param email         correo del usuario
	 * @param codigo        codigo de 6 digitos enviado por correo
	 * @param nuevaPassword nueva contrasena a establecer
	 * @return codigo de resultado
	 */
	public int confirmarReset(String email, String codigo, String nuevaPassword) {
		if (email == null || email.isBlank()) {
			return 1;
		}
		if (codigo == null || codigo.isBlank()) {
			return 2;
		}
		if (nuevaPassword == null || !PASSWORD_PATTERN.matcher(nuevaPassword).matches()) {
			return 4;
		}

		String normalizedEmail = email.trim().toLowerCase();
		Optional<User> found = userRepo.findByEmail(normalizedEmail);
		if (found.isEmpty()) {
			return 1;
		}

		User user = found.get();

		if (user.getVerificationCode() == null || !codigo.trim().equals(user.getVerificationCode())) {
			return 2;
		}

		if (user.getVerificationExpiry() == null
				|| LocalDateTime.now().isAfter(user.getVerificationExpiry())) {
			return 3;
		}

		user.setPassword(passwordEncoder.encode(nuevaPassword));
		user.setVerificationCode(null);
		user.setVerificationExpiry(null);
		userRepo.save(user);
		return 0;
	}

	/**
	 * Genera un codigo numerico aleatorio de 6 digitos.
	 *
	 * @return cadena de 6 digitos
	 */
	private String generarCodigo() {
		int numero = 100000 + SECURE_RANDOM.nextInt(900000);
		return String.valueOf(numero);
	}
}