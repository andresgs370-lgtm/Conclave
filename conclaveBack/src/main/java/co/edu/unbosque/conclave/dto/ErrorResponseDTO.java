package co.edu.unbosque.conclave.dto;
import java.time.LocalDateTime;
import java.util.Objects;
/**
 * Respuesta de error estandarizada para todos los endpoints.
 * Producida por {@code GlobalExceptionHandler}.
 */
public class ErrorResponseDTO {
	/** Marca temporal del error. */
	private LocalDateTime timestamp;

	/** Código de estado HTTP. */
	private int status;

	/** Nombre del error HTTP (p.ej. {@code "Not Found"}). */
	private String error;

	/** Mensaje descriptivo del error. */
	private String message;

	/** Ruta del endpoint que generó el error. */
	private String path;
	
	/** Constructor por defecto. */
	public ErrorResponseDTO() {
	}

	/**
	 * Constructor completo. Establece el timestamp al momento de la creación.
	 *
	 * @param status  código de estado HTTP
	 * @param error   nombre del error HTTP
	 * @param message mensaje descriptivo
	 * @param path    ruta del endpoint
	 */
	public ErrorResponseDTO(int status, String error, String message, String path) {
		this.timestamp = LocalDateTime.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int hashCode() {
		return Objects.hash(error, message, path, Integer.valueOf(status), timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorResponseDTO other = (ErrorResponseDTO) obj;
		return Objects.equals(error, other.error) && Objects.equals(message, other.message)
				&& Objects.equals(path, other.path) && status == other.status
				&& Objects.equals(timestamp, other.timestamp);
	}

	@Override
	public String toString() {
		return "ErrorResponseDTO [timestamp=" + timestamp + ", status=" + status + ", error=" + error + ", message="
				+ message + ", path=" + path + "]";
	}
	
	
}
