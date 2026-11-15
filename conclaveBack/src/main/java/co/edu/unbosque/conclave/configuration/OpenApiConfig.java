package co.edu.unbosque.conclave.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar la API REST de Conclave.
 * Define la información general de la API, el esquema de seguridad JWT
 * y los componentes de respuesta reutilizables.
 */
@Configuration
public class OpenApiConfig {

	/**
	 * Define la configuración personalizada de OpenAPI para la API de Conclave.
	 *
	 * @return objeto {@link OpenAPI} con la configuración completa
	 */
	@Bean
	public OpenAPI customOpenAPI() {

		String mainDescription =
				"<h2>API de Conclave — Guía de uso</h2>"
				+ "<p>Esta API permite enviar un mismo prompt a múltiples proveedores de IA "
				+ "(OpenAI, Claude, Gemini, OpenRouter) y comparar sus respuestas en texto, "
				+ "imagen, vídeo y audio.</p>"
				+ "<h3>Autenticación JWT:</h3>"
				+ "<ol>"
				+ "  <li>Registra un usuario en <code>/auth/register</code> o usa las credenciales predeterminadas.</li>"
				+ "  <li>Inicia sesión en <code>/auth/login</code> para obtener tu token JWT.</li>"
				+ "  <li>Haz clic en <strong>Authorize</strong> e introduce: <code>Bearer &lt;token&gt;</code>.</li>"
				+ "</ol>"
				+ "<h3>Roles:</h3>"
				+ "<ul>"
				+ "  <li><strong>USER</strong>: puede enviar prompts, ver su historial y su perfil.</li>"
				+ "  <li><strong>ADMIN</strong>: acceso completo — gestión de usuarios, API keys e historial global.</li>"
				+ "</ul>"
				+ "<h3>Códigos HTTP comunes:</h3>"
				+ "<ul>"
				+ "  <li><strong>200/201</strong>: operación exitosa.</li>"
				+ "  <li><strong>400</strong>: datos incorrectos.</li>"
				+ "  <li><strong>401</strong>: no autenticado.</li>"
				+ "  <li><strong>403</strong>: sin permisos.</li>"
				+ "  <li><strong>404</strong>: recurso no encontrado.</li>"
				+ "  <li><strong>503</strong>: API key del proveedor no configurada.</li>"
				+ "</ul>";

		String securityDescription =
				"Autenticación mediante JWT (JSON Web Token)."
				+ "<p>Pasos:</p>"
				+ "<ol>"
				+ "  <li>Obtén un token en <code>/auth/login</code>.</li>"
				+ "  <li>Haz clic en el botón <strong>Authorize</strong>.</li>"
				+ "  <li>Escribe: <code>Bearer &lt;tu_token_jwt&gt;</code>.</li>"
				+ "  <li>Haz clic en <strong>Authorize</strong> y luego en <strong>Close</strong>.</li>"
				+ "</ol>";

		io.swagger.v3.oas.models.info.Info info =
				new io.swagger.v3.oas.models.info.Info()
						.title("API de Conclave")
						.version("1.0")
						.description(mainDescription)
						.contact(new io.swagger.v3.oas.models.info.Contact()
								.name("Equipo de Desarrollo")
								.email("soporte@conclave.com")
								.url("https://github.com/tu-usuario/conclave"))
						.license(new io.swagger.v3.oas.models.info.License()
								.name("Licencia MIT")
								.url("https://opensource.org/licenses/MIT"));

		io.swagger.v3.oas.models.security.SecurityScheme securityScheme =
				new io.swagger.v3.oas.models.security.SecurityScheme()
						.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description(securityDescription);

		return new OpenAPI()
				.info(info)
				.components(new Components()
						.addSecuritySchemes("bearerAuth", securityScheme)
						.addResponses("UnauthorizedError",
								new ApiResponse()
										.description("No autenticado — Token JWT inválido o expirado")
										.content(new Content()
												.addMediaType("application/json",
														new MediaType().addExamples("error",
																new Example().value(
																		"{\"error\":\"No autorizado\","
																		+ "\"mensaje\":\"Token inválido o expirado\"}")))))
						.addResponses("ForbiddenError",
								new ApiResponse()
										.description("Sin permisos suficientes")
										.content(new Content()
												.addMediaType("application/json",
														new MediaType().addExamples("error",
																new Example().value(
																		"{\"error\":\"Acceso prohibido\","
																		+ "\"mensaje\":\"No tienes permisos para esta operación\"}")))))
						.addResponses("NotFoundError",
								new ApiResponse()
										.description("Recurso no encontrado")
										.content(new Content()
												.addMediaType("application/json",
														new MediaType().addExamples("error",
																new Example().value(
																		"{\"error\":\"No encontrado\","
																		+ "\"mensaje\":\"El recurso solicitado no existe\"}"))))));
	}
}
