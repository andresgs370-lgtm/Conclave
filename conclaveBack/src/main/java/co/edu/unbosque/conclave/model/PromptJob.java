package co.edu.unbosque.conclave.model;

import co.edu.unbosque.conclave.dto.AIResponseDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representación en memoria de un job activo.
 *
 * <p>No es una entidad JPA — vive en {@code PromptJobManager}.
 * Se expira automáticamente cuando {@code expiresAt} es anterior a la hora actual.</p>
 *
 * <p>Con un único proveedor (OpenRouter), {@code result} almacena directamente
 * la respuesta recibida. {@code status} es {@code volatile} para visibilidad
 * entre hilos del {@code AsyncConfig.aiExecutor}.</p>
 */
public class PromptJob implements Serializable{
	
	/** Número de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Identificador UUID del job. */
	private String jobId;

	/** Nombre del usuario que generó el job. */
	private String username;

	/** ID del registro de {@link PromptHistory} asociado. */
	private Long historyId;

	/** Request original del usuario. */
	private PromptRequestDTO request;

	/**
	 * Estado actual del job. Marcado {@code volatile} para visibilidad entre hilos.
	 */
	private volatile JobStatus status;

	/**
	 * Respuesta de OpenRouter. {@code null} mientras el job no ha terminado.
	 */
	private AIResponseDTO result;

	/** Fecha y hora de creación del job. */
	private LocalDateTime createdAt;

	/** Fecha y hora de expiración; tras esta marca, el job se purga. */
	private LocalDateTime expiresAt;

	/** Constructor por defecto. */
	public PromptJob() {
	}

	/**
	 * Crea un job con todos sus campos iniciales.
	 * @param jobId     identificador único UUID
	 * @param username  nombre del usuario propietario
	 * @param historyId ID del historial JPA asociado
	 * @param request   request original del cliente
	 * @param createdAt marca temporal de creación
	 * @param expiresAt marca temporal de expiración
	 */
	public PromptJob(String jobId, String username, Long historyId, PromptRequestDTO request,
			LocalDateTime createdAt, LocalDateTime expiresAt) {
		this();
		this.jobId = jobId;
		this.username = username;
		this.historyId = historyId;
		this.request = request;
		this.status = JobStatus.PENDING;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}
	
	/**
	 * Registra la respuesta de OpenRouter.
	 * @param response respuesta recibida de OpenRouter
	 */
	public void addResult(AIResponseDTO response) {
		this.result = response;
	}

	/**
	 * Indica si OpenRouter ya respondió (con éxito o con error).
	 * @return {@code true} si ya hay una respuesta
	 */
	public boolean isFinished() {
		return result != null;
	}

	/**
	 * Indica si el job ha superado su tiempo de vida.
	 * @return {@code true} si la hora actual es posterior a {@code expiresAt}
	 */
	public boolean isExpired() {
		return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	public PromptRequestDTO getRequest() {
		return request;
	}

	public void setRequest(PromptRequestDTO request) {
		this.request = request;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public AIResponseDTO getResult() {
		return result;
	}

	public void setResult(AIResponseDTO result) {
		this.result = result;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdAt, expiresAt, historyId, jobId, request, result, status, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptJob other = (PromptJob) obj;
		return Objects.equals(createdAt, other.createdAt) && Objects.equals(expiresAt, other.expiresAt)
				&& Objects.equals(historyId, other.historyId) && Objects.equals(jobId, other.jobId)
				&& Objects.equals(request, other.request) && Objects.equals(result, other.result)
				&& status == other.status && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "PromptJob [jobId=" + jobId + ", username=" + username + ", historyId=" + historyId + ", request="
				+ request + ", status=" + status + ", result=" + result + ", createdAt=" + createdAt + ", expiresAt="
				+ expiresAt + "]";
	}
	
	
}
