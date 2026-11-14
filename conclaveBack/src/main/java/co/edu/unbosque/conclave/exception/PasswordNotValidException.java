package co.edu.unbosque.conclave.exception;

/**
 * Excepción que se lanza cuando una contraseña no cumple con los requisitos de seguridad.
 * <p>Una contraseña válida debe tener mínimo 8 caracteres, al menos una letra minúscula,
 * al menos una letra mayúscula, al menos un número y al menos un símbolo.</p>
 */
public class PasswordNotValidException extends Exception {

	/** Número de serie para la serialización. */
	private static final long serialVersionUID = -1290628531913444044L;

	/**
	 * Constructor por defecto que inicializa la excepción con un mensaje de error estándar.
	 */
	public PasswordNotValidException() {
		super("La contraseña no cumple con el estándar "
				+ "(mínimo 8 caracteres, al menos una letra minúscula, "
				+ "al menos una letra mayúscula, "
				+ "al menos un número y al menos un símbolo).");
	}
	
}
