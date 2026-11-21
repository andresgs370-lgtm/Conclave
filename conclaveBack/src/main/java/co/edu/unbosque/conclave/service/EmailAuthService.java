package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Optional<User> found = userRepo.findByEmail(email);
        if (found.isEmpty()) {
            return 1;
        }

        User user = found.get();
        String codigo = generarCodigo();
        user.setVerificationCode(codigo);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        userRepo.save(user);

        boolean enviado = emailService.enviarCodigoVerificacion(email, codigo);
        if (!enviado) {
            LOGGER.log(Level.SEVERE, "No se pudo enviar el correo de verificacion a {0}", email);
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
        Optional<User> found = userRepo.findByEmail(email);
        if (found.isEmpty()) {
            return 1;
        }

        User user = found.get();

        if (!codigo.equals(user.getVerificationCode())) {
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
     * Resultados posibles:
     * 
     *   {@code 0} — codigo enviado correctamente.
     *   {@code 1} — usuario no encontrado con ese correo.
     *   {@code 2} — fallo el envio del correo.
     * 
     *
     * @param email correo del usuario que solicita el reset
     * @return codigo de resultado
     */
    public int solicitarReset(String email) {
        Optional<User> found = userRepo.findByEmail(email);
        if (found.isEmpty()) {
            return 1;
        }

        User user = found.get();
        String codigo = generarCodigo();
        user.setVerificationCode(codigo);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        userRepo.save(user);

        boolean enviado = emailService.enviarCodigoReset(email, codigo);
        if (!enviado) {
            LOGGER.log(Level.SEVERE, "No se pudo enviar el correo de reset a {0}", email);
            return 2;
        }
        return 0;
    }

    /**
     * Confirma el restablecimiento de contrasena validando el codigo y
     * guardando la nueva contrasena cifrada con BCrypt.
     *
     * <p>Resultados posibles:</p>
     * <ul>
     *   <li>{@code 0} — contrasena actualizada correctamente.</li>
     *   <li>{@code 1} — usuario no encontrado.</li>
     *   <li>{@code 2} — codigo incorrecto.</li>
     *   <li>{@code 3} — codigo expirado.</li>
     * </ul>
     *
     * @param email          correo del usuario
     * @param codigo         codigo de reset ingresado
     * @param nuevaPassword  nueva contrasena en texto plano
     * @return codigo de resultado
     */
    public int confirmarReset(String email, String codigo, String nuevaPassword) {
        Optional<User> found = userRepo.findByEmail(email);
        if (found.isEmpty()) {
            return 1;
        }

        User user = found.get();

        if (!codigo.equals(user.getVerificationCode())) {
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
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}
