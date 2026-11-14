package co.edu.unbosque.conclave.exception;

import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.OutputType;

/**
 * Excepción que se lanza cuando se solicita un tipo de salida que el proveedor
 * de IA indicado no soporta.
 */
public class UnsupportedOutputTypeException extends Exception {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción indicando el proveedor y el tipo de salida no soportado.
	 *
	 * @param provider   proveedor que no soporta el tipo de salida
	 * @param outputType tipo de salida no soportado
	 */
	public UnsupportedOutputTypeException(AIProvider provider, OutputType outputType) {
		super("El proveedor " + provider.getDisplayName()
				+ " no soporta el tipo de salida: " + outputType);
	}

}
