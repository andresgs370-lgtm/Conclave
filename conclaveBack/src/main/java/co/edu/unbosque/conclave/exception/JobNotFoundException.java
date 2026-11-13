package co.edu.unbosque.conclave.exception;

/**
* Excepción que se lanza cuando no se encuentra un job con el ID indicado,
* o cuando el job no pertenece al usuario que lo solicita.
*/
public class JobNotFoundException extends Exception {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el ID del job no encontrado.
	 *
	 * @param jobId identificador del job no encontrado
	 */
	public JobNotFoundException(String jobId) {
		super("No se encontró el trabajo con ID: " + jobId);
	}
}
