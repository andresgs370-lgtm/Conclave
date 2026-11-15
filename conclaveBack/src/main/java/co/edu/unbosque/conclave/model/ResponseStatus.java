package co.edu.unbosque.conclave.model;

/**
 * Estado de la respuesta individual de un proveedor de IA.
 */
public enum ResponseStatus {
    /** La generación fue exitosa. */
    SUCCESS,
    /** El proveedor devolvió un error. */
    FAILED,
    /** El proveedor no soporta el OutputType solicitado. */
    UNSUPPORTED,
    /** La petición superó el tiempo de espera. */
    TIMEOUT
}
