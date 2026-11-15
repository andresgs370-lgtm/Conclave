package co.edu.unbosque.conclave.exception;

import co.edu.unbosque.conclave.model.AIProvider;

/**
 * Excepción que se lanza cuando se intenta usar un proveedor de IA
 * cuya API key no ha sido configurada por el administrador.
 */
public class ApiKeyNotConfiguredException extends Exception {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción indicando qué proveedor no tiene clave configurada.
	 *
	 * @param provider proveedor sin API key configurada
	 */
	public ApiKeyNotConfiguredException(AIProvider provider) {
		super("La API key del proveedor " + provider.getDisplayName()
				+ " no está configurada. Contacta al administrador.");
	}
}
