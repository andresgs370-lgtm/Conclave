package co.edu.unbosque.conclave.exception;

/**
 * Excepción que se lanza cuando se solicita la descarga de un contenido multimedia
 * que ya ha expirado y fue eliminado de la memoria del servidor.
 */
public class MediaExpiredException extends Exception {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción indicando qué job ha expirado.
	 *
	 * @param jobId identificador del job cuyos datos han expirado
	 */
	public MediaExpiredException(String jobId) {
		super("El contenido multimedia del trabajo " + jobId
				+ " ha expirado y ya no está disponible.");
	}

}
