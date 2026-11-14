package co.edu.unbosque.conclave.dto;

import co.edu.unbosque.conclave.model.JobStatus;
import co.edu.unbosque.conclave.model.MediaFormat;
import co.edu.unbosque.conclave.model.OutputType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO de respuesta para el historial de prompts de un usuario.
 *
 * <p>Con OpenRouter como único proveedor, cada registro de historial
 * tiene exactamente una respuesta.</p>
 */
public class PromptHistoryDTO {
	/** Identificador único del registro de historial. */
	private Long id;

	/** Nombre de usuario propietario del historial. */
	private String username;

	/** Texto del prompt enviado. */
	private String promptText;

	/** Tipo de salida solicitado. */
	private OutputType outputType;

	/** Formato de archivo solicitado. */
	private MediaFormat format;

	/** Modelo de OpenRouter utilizado. */
	private String model;

	/** Estado final del job. */
	private JobStatus status;

	/** Fecha y hora de creación del registro. */
	private LocalDateTime createdAt;

	/** Respuesta de OpenRouter (sin datos binarios). */
	private AIResponseDTO response;
	
	/** Constructor por defecto. */
	public PromptHistoryDTO() {
	}

	public PromptHistoryDTO(Long id, String username, String promptText, OutputType outputType, MediaFormat format,
			String model, JobStatus status, LocalDateTime createdAt, AIResponseDTO response) {
		super();
		this.id = id;
		this.username = username;
		this.promptText = promptText;
		this.outputType = outputType;
		this.format = format;
		this.model = model;
		this.status = status;
		this.createdAt = createdAt;
		this.response = response;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPromptText() {
		return promptText;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
	}

	public MediaFormat getFormat() {
		return format;
	}

	public void setFormat(MediaFormat format) {
		this.format = format;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public AIResponseDTO getResponse() {
		return response;
	}

	public void setResponse(AIResponseDTO response) {
		this.response = response;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdAt, format, id, model, outputType, promptText, response, status, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptHistoryDTO other = (PromptHistoryDTO) obj;
		return Objects.equals(createdAt, other.createdAt) && format == other.format && Objects.equals(id, other.id)
				&& Objects.equals(model, other.model) && outputType == other.outputType
				&& Objects.equals(promptText, other.promptText) && Objects.equals(response, other.response)
				&& status == other.status && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "PromptHistoryDTO [id=" + id + ", username=" + username + ", promptText=" + promptText + ", outputType="
				+ outputType + ", format=" + format + ", model=" + model + ", status=" + status + ", createdAt="
				+ createdAt + ", response=" + response + "]";
	}
	
	
}
