package co.edu.unbosque.conclave.model;

/**
 * Estados del ciclo de vida de un job de generación de contenido.
 */
public enum JobStatus {
    /** Recibido, pendiente de procesamiento. */
    PENDING,
    /** Al menos un proveedor está trabajando. */
    PROCESSING,
    /** Todos los proveedores respondieron con éxito. */
    COMPLETED,
    /** Al menos uno respondió con éxito, pero no todos. */
    PARTIAL,
    /** Todos los proveedores fallaron. */
    FAILED,
    /** El job expiró y sus resultados fueron eliminados. */
    EXPIRED
}
