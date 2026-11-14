package co.edu.unbosque.conclave.dto;

import co.edu.unbosque.conclave.model.JobStatus;
import co.edu.unbosque.conclave.model.OutputType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Snapshot del estado actual de un job de generación en memoria.
 * Se devuelve al cliente al enviar un prompt y al consultar su estado.
 *
 * <p>Con OpenRouter como único proveedor, el job tiene una sola respuesta
 * posible en lugar de una lista.</p>
 */
public class PromptJobDTO {
	
	/** Identificador UUID del job. */
	private String jobId;

	/** Estado actual del job. */
	private JobStatus status;

	/** Tipo de salida solicitado. */
	private OutputType outputType;

	/** Texto del prompt original. */
	private String promptText;

	/** Modelo de OpenRouter utilizado. */
	private String model;

	/** Fecha y hora de creación del job. */
	private LocalDateTime createdAt;

	/** Fecha y hora de expiración del job. */
	private LocalDateTime expiresAt;

	/** Respuesta de OpenRouter (puede ser {@code null} si aún está procesando). */
	private AIResponseDTO result;
	
	/** Constructor por defecto. */
	public PromptJobDTO() {
	}

	public PromptJobDTO(String jobId, JobStatus status, OutputType outputType, String promptText, String model,
			LocalDateTime createdAt, LocalDateTime expiresAt, AIResponseDTO result) {
		super();
		this.jobId = jobId;
		this.status = status;
		this.outputType = outputType;
		this.promptText = promptText;
		this.model = model;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.result = result;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
	}

	public String getPromptText() {
		return promptText;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public AIResponseDTO getResult() {
		return result;
	}

	public void setResult(AIResponseDTO result) {
		this.result = result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdAt, expiresAt, jobId, model, outputType, promptText, result, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptJobDTO other = (PromptJobDTO) obj;
		return Objects.equals(createdAt, other.createdAt) && Objects.equals(expiresAt, other.expiresAt)
				&& Objects.equals(jobId, other.jobId) && Objects.equals(model, other.model)
				&& outputType == other.outputType && Objects.equals(promptText, other.promptText)
				&& Objects.equals(result, other.result) && status == other.status;
	}

	@Override
	public String toString() {
		return "PromptJobDTO [jobId=" + jobId + ", status=" + status + ", outputType=" + outputType + ", promptText="
				+ promptText + ", model=" + model + ", createdAt=" + createdAt + ", expiresAt=" + expiresAt
				+ ", result=" + result + "]";
	}
	
	
}
