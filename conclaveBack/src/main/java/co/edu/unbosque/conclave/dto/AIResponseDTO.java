package co.edu.unbosque.conclave.dto;

import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.MediaFormat;
import co.edu.unbosque.conclave.model.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * Respuesta normalizada de un proveedor de IA para un prompt dado.
 *
 * <p>Puede contener texto, datos binarios de media, o ambos.
 * {@code mediaData} lleva {@link JsonIgnore} porque los bytes de media
 * <strong>nunca</strong> se serializan en JSON: se entregan exclusivamente a través
 * del endpoint de descarga {@code /prompt/{jobId}/download}.</p>
 */
public class AIResponseDTO {
	/** Proveedor de IA que generó la respuesta. */
	private AIProvider provider;

	/** Nombre del modelo concreto utilizado. */
	private String model;

	/** Estado de la respuesta. */
	private ResponseStatus status;

	/** Código HTTP devuelto por el proveedor. */
	private int httpStatus;

	/** Texto generado (solo cuando {@code outputType == TEXT}). */
	private String textContent;

	/** Formato del archivo generado. */
	private MediaFormat format;

	/** MIME type del contenido (p.ej. {@code "image/png"}). */
	private String mimeType;

	/** URL temporal de descarga si el proveedor la expone directamente. */
	private String downloadUrl;

	/** Tamaño en bytes del contenido multimedia. */
	private long mediaSizeBytes;

	/** Mensaje de error del proveedor cuando {@code status != SUCCESS}. */
	private String errorMessage;

	/** Duración total de la llamada al proveedor en milisegundos. */
	private long durationMs;
	
	/** Constructor por defecto. */
	public AIResponseDTO() {
	}

	public AIResponseDTO(AIProvider provider, String model, ResponseStatus status, int httpStatus, String textContent,
			MediaFormat format, String mimeType, String downloadUrl, long mediaSizeBytes, String errorMessage,
			long durationMs) {
		super();
		this.provider = provider;
		this.model = model;
		this.status = status;
		this.httpStatus = httpStatus;
		this.textContent = textContent;
		this.format = format;
		this.mimeType = mimeType;
		this.downloadUrl = downloadUrl;
		this.mediaSizeBytes = mediaSizeBytes;
		this.errorMessage = errorMessage;
		this.durationMs = durationMs;
	}

	public AIProvider getProvider() {
		return provider;
	}

	public void setProvider(AIProvider provider) {
		this.provider = provider;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public void setStatus(ResponseStatus status) {
		this.status = status;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public MediaFormat getFormat() {
		return format;
	}

	public void setFormat(MediaFormat format) {
		this.format = format;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public long getMediaSizeBytes() {
		return mediaSizeBytes;
	}

	public void setMediaSizeBytes(long mediaSizeBytes) {
		this.mediaSizeBytes = mediaSizeBytes;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public long getDurationMs() {
		return durationMs;
	}

	public void setDurationMs(long durationMs) {
		this.durationMs = durationMs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(downloadUrl, Long.valueOf(durationMs), errorMessage, format, Integer.valueOf(httpStatus),
				Long.valueOf(mediaSizeBytes), mimeType, model, provider, status, textContent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AIResponseDTO other = (AIResponseDTO) obj;
		return Objects.equals(downloadUrl, other.downloadUrl) && durationMs == other.durationMs
				&& Objects.equals(errorMessage, other.errorMessage) && format == other.format
				&& httpStatus == other.httpStatus && mediaSizeBytes == other.mediaSizeBytes
				&& Objects.equals(mimeType, other.mimeType) && Objects.equals(model, other.model)
				&& provider == other.provider && status == other.status
				&& Objects.equals(textContent, other.textContent);
	}

	@Override
	public String toString() {
		return "AIResponseDTO [provider=" + provider + ", model=" + model + ", status=" + status + ", httpStatus="
				+ httpStatus + ", textContent=" + textContent + ", format=" + format + ", mimeType=" + mimeType
				+ ", downloadUrl=" + downloadUrl + ", mediaSizeBytes=" + mediaSizeBytes + ", errorMessage="
				+ errorMessage + ", durationMs=" + durationMs + "]";
	}
	
	
}
