package co.edu.unbosque.conclave.exception;

import co.edu.unbosque.conclave.model.AIProvider;

/**
 * Excepción que se lanza cuando un proveedor de IA devuelve un error HTTP.
 * <p>Extiende {@link RuntimeException} porque se lanza dentro de
 * {@code CompletableFuture} donde las checked exceptions no son prácticas.</p>
 */
public class AIProviderException extends RuntimeException {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Proveedor de IA que generó el error. */
	private final AIProvider provider;

	/** Código HTTP devuelto por el proveedor. */
	private final int httpStatus;

	/**
	 * Crea una excepción de proveedor de IA con mensaje descriptivo.
	 *
	 * @param provider   proveedor que generó el error
	 * @param httpStatus código HTTP de la respuesta del proveedor
	 * @param message    mensaje descriptivo del error
	 */
	public AIProviderException(AIProvider provider, int httpStatus, String message) {
		super(message);
		this.provider = provider;
		this.httpStatus = httpStatus;
	}

	/** @return proveedor de IA que generó el error */
	public AIProvider getProvider() { return provider; }

	/** @return código HTTP devuelto por el proveedor */
	public int getHttpStatus() { return httpStatus; }
	
}
