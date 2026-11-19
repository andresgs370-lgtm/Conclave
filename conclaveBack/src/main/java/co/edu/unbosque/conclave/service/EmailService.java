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
 * Servicio encargado del envío de correos electrónicos mediante Jakarta Mail.
 * Utiliza {@link JavaMailSender} configurado con las propiedades SMTP definidas
 * en {@code application.properties} bajo el prefijo {@code spring.mail}.
 * Este servicio solo envía correos. No genera códigos, no guarda nada en la
 * base de datos y no maneja lógica de autenticación. Esas responsabilidades
 * pertenecen a quien implemente la verificación de login.
 */
@Service
public class EmailService {

	/** Logger de la clase. */
	private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

	/** Remitente de los correos (leído desde application.properties). */
	@Value("${spring.mail.username}")
	private String remitente;

	/** Componente de Spring para enviar correos. */
	@Autowired
	private JavaMailSender mailSender;

	/** Constructor por defecto. */
	public EmailService() {
	}

	/**
	 * Envía un correo electrónico en formato HTML al destinatario indicado.
	 *
	 * Si el envío falla por un error SMTP o de configuración, el error se registra
	 * en el log y el método retorna {@code false} sin lanzar excepción, para que el
	 * llamador decida cómo manejarlo. 
	 *
	 * @param destinatario dirección de correo del receptor
	 * @param asunto       asunto del correo
	 * @param cuerpoHtml   contenido del correo en formato HTML
	 * @return {@code true} si el correo se envió correctamente, {@code false} si
	 *         ocurrió un error durante el envío
	 */
	public boolean enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

			helper.setFrom(remitente);
			helper.setTo(destinatario);
			helper.setSubject(asunto);
			helper.setText(cuerpoHtml, true);

			mailSender.send(mensaje);
			return true;

		} catch (MessagingException ex) {
			LOGGER.log(Level.SEVERE, "Error al enviar correo a {0}: {1}",
					new Object[] { destinatario, ex.getMessage() });
			return false;
		}
	}
	public boolean enviarCodigoVerificacion(String destinatario, String codigo) {
		String asunto = "Conclave - Código de verificación";
		String cuerpoHtml = construirHtmlCodigo(codigo);
		return enviarCorreo(destinatario, asunto, cuerpoHtml);
	}

	/**
	 * Construye el HTML del correo de verificación con el código proporcionado.
	 *
	 * @param codigo código de verificación de 6 dígitos
	 * @return cadena HTML lista para incluir en el cuerpo del correo
	 */
	private String construirHtmlCodigo(String codigo) {
		return """
				<div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
				    <h2 style="color: #4A90D9;">Verificación de inicio de sesión</h2>
				    <p>Tu código de verificación para ingresar a <strong>Conclave</strong> es:</p>
				    <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
				                color: #4A90D9; text-align: center; padding: 16px 0;">
				        %s
				    </div>
				    <p style="color: #888;">Este código expira en 10 minutos.<br>
				    Si no fuiste tú, ignora este mensaje.</p>
				</div>
				""".formatted(codigo);
	}
}
