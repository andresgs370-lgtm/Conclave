package co.edu.unbosque.conclave.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio encargado del envio de correos electronicos mediante Jakarta Mail.
 *
 * <p>Utiliza {@link JavaMailSender} configurado con las propiedades SMTP
 * definidas en {@code application.properties} bajo el prefijo
 * {@code spring.mail}.</p>
 *
 * <p>Este servicio solo envia correos. No genera codigos, no guarda nada
 * en la base de datos y no maneja logica de autenticacion. Esas
 * responsabilidades pertenecen a quien implemente la verificacion de login.</p>
 */
@Service
public class EmailService {

    /** Logger de la clase. */
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    /** Asunto del correo de verificacion de login. */
    private static final String ASUNTO_VERIFICACION = "Conclave - Codigo de verificacion";

    /** Remitente de los correos (leido desde application.properties). */
    @Value("${spring.mail.username}")
    private String remitente;

    /** Componente de Spring para enviar correos. */
    @Autowired
    private JavaMailSender mailSender;

    /** Constructor por defecto. */
    public EmailService() {
    }

    /**
     * Envia un correo electronico de texto plano al destinatario indicado.
     *
     * <p>Si el envio falla por un error SMTP o de configuracion, el error
     * se registra en el log y el metodo retorna {@code false} sin lanzar
     * excepcion, para que el llamador decida como manejarlo.</p>
     *
     * @param destinatario direccion de correo del receptor
     * @param asunto       asunto del correo
     * @param cuerpo       contenido del correo en texto plano
     * @return {@code true} si el correo se envio correctamente,
     *         {@code false} si ocurrio un error durante el envio
     */
    public boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, false);

            mailSender.send(mensaje);
            return true;

        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al enviar correo a {0}: {1}",
                    new Object[]{destinatario, ex.getMessage()});
            return false;
        }
    }

    /**
     * Envia un correo de verificacion de login con el codigo proporcionado.
     *
     * El cuerpo del correo es texto plano. El diseno visual del mensaje
     * es responsabilidad del equipo de frontend, que puede personalizar
     * la presentacion del codigo en la interfaz de usuario.
     *
     * Ejemplo de uso:
     * <pre>{@code
     * boolean enviado = emailService.enviarCodigoVerificacion(
     *     usuario.getEmail(), "482910");
     * if (!enviado) {
     *     // manejar el error
     * }
     * }</pre>
     *
     * @param destinatario direccion de correo del usuario que inicio sesion
     * @param codigo       codigo de verificacion de 6 digitos generado previamente
     * @return {@code true} si el correo se envio correctamente,
     *         {@code false} si ocurrio un error durante el envio
     */
    public boolean enviarCodigoVerificacion(String destinatario, String codigo) {
        String cuerpo = construirCuerpo(codigo);
        return enviarCorreo(destinatario, ASUNTO_VERIFICACION, cuerpo);
    }

    /**
     * Construye el cuerpo en texto plano del correo de verificacion.
     *
     * <p>El diseno visual y la presentacion final del codigo al usuario
     * es responsabilidad del frontend. Este metodo solo genera el texto
     * que viaja en el correo.</p>
     *
     * @param codigo codigo de verificacion de 6 digitos
     * @return texto plano listo para incluir en el cuerpo del correo
     */
    private String construirCuerpo(String codigo) {
        return "Conclave - Verificacion de inicio de sesion\n\n"
                + "Tu codigo de verificacion es: " + codigo + "\n\n"
                + "Este codigo expira en 10 minutos.\n"
                + "Si no fuiste tu, ignora este mensaje.";
    }
}