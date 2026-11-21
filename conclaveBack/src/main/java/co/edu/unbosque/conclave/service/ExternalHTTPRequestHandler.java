package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.exception.AIProviderException;
import co.edu.unbosque.conclave.model.AIProvider;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Componente para manejar peticiones HTTP a servicios externos (proveedores de IA).
 *
 * <p>Utiliza el cliente HTTP nativo de Java ({@link HttpClient}) configurado con
 * HTTP/2 y un timeout de conexión de 10 segundos. Todas las llamadas son asíncronas
 * mediante {@code sendAsync}.</p>
 */
@Component
public class ExternalHTTPRequestHandler {

	/**
	 * Tiempo de espera de conexión para el cliente HTTP.
	 */
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

	/**
	 * Cliente HTTP compartido y reutilizable.
	 * HTTP/2 con timeout de conexión de {@value} segundos.
	 */
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.connectTimeout(CONNECT_TIMEOUT)
			.build();

	/** Constructor por defecto. */
	public ExternalHTTPRequestHandler() {
	}

	/**
	 * Realiza una petición POST asíncrona con cuerpo JSON.
	 *
	 * @param url      URL del endpoint
	 * @param headers  cabeceras HTTP (clave → valor)
	 * @param jsonBody cuerpo de la petición en formato JSON
	 * @return {@code CompletableFuture} con la respuesta HTTP (cuerpo como {@code String})
	 */
	public CompletableFuture<HttpResponse<String>> doPostAsync(
			String url, Map<String, String> headers, String jsonBody) {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(60))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody));

		headers.forEach(builder::header);

		return HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	/**
	 * Realiza una petición GET asíncrona.
	 *
	 * @param url     URL del endpoint
	 * @param headers cabeceras HTTP (clave → valor)
	 * @return {@code CompletableFuture} con la respuesta HTTP (cuerpo como {@code String})
	 */
	public CompletableFuture<HttpResponse<String>> doGetAsync(
			String url, Map<String, String> headers) {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(60))
				.GET();

		headers.forEach(builder::header);

		return HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	/**
	 * Realiza una petición GET asíncrona que descarga contenido binario.
	 *
	 * @param url     URL del recurso a descargar
	 * @param headers cabeceras HTTP (clave → valor)
	 * @return {@code CompletableFuture} con la respuesta HTTP (cuerpo como {@code byte[]})
	 */
	public CompletableFuture<HttpResponse<byte[]>> doGetBytesAsync(
			String url, Map<String, String> headers) {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(60))
				.GET();

		headers.forEach(builder::header);

		return HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
	}

	/**
	 * Realiza una petición POST asíncrona con cuerpo JSON y respuesta binaria,
	 * útil para proveedores que devuelven audio o imágenes directamente.
	 *
	 * @param url      URL del endpoint
	 * @param headers  cabeceras HTTP (clave → valor)
	 * @param jsonBody cuerpo de la petición en formato JSON
	 * @return {@code CompletableFuture} con la respuesta HTTP (cuerpo como {@code byte[]})
	 */
	public CompletableFuture<HttpResponse<byte[]>> doPostBytesAsync(
			String url, Map<String, String> headers, String jsonBody) {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(90))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody));

		headers.forEach(builder::header);

		return HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
	}

	/**
	 * Verifica que la respuesta HTTP sea exitosa (2xx).
	 * Lanza {@link AIProviderException} si el código de estado no es 2xx.
	 *
	 * @param response respuesta HTTP a verificar
	 * @param provider proveedor de IA que generó la respuesta (para el mensaje de error)
	 * @throws AIProviderException si el código de estado no es 2xx
	 */
	public void ensureSuccess(HttpResponse<?> response, AIProvider provider) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new AIProviderException(provider, response.statusCode(),
					"El proveedor " + provider.getDisplayName()
							+ " respondió con HTTP " + response.statusCode());
		}
	}

}