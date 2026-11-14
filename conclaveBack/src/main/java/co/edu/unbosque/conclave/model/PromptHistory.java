package co.edu.unbosque.conclave.model;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidad que registra de forma persistente cada prompt enviado por un usuario.
 *
 * <p>Con OpenRouter como único proveedor, cada registro de historial tiene
 * exactamente un {@link ProviderResponseLog} asociado.</p>
 */
@Entity
@Table(name = "prompt_history")
public class PromptHistory implements Serializable {
	/** Número de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Identificador único del registro de historial. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Usuario que generó el prompt. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** Texto del prompt enviado a OpenRouter. */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String promptText;

	/** Tipo de salida solicitado (texto, imagen, vídeo, audio). */
	@Column(nullable = false, length = 10)
	private OutputType outputType;

	/** Formato de archivo deseado; compatible con {@code outputType}. */
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private MediaFormat format;

	/** Modelo de OpenRouter utilizado entre los que trae. */
	@Column(length = 100)
	private String model;

	/** Estado final del job. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private JobStatus status;

	/** Fecha y hora de creación. Se asigna automáticamente al persistir. */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** Fecha y hora en que el job finalizó */
	@Column
	private LocalDateTime completedAt;

	/**
	 * Log de la respuesta de OpenRouter
	 * Relación lazy — no se incluye en {@code toString} para evitar
	 * {@code LazyInitializationException}
	 */
	@OneToOne(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true,
			fetch = FetchType.LAZY)
	private ProviderResponseLog responseLog;
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.status == null) {
			this.status = JobStatus.PENDING;
		}
	}
	
	/** Constructor por defecto */
	public PromptHistory() {
	}

	/**
	 * Crea un registro de historial básico
	 * @param user       usuario que realizó el prompt
	 * @param promptText texto del prompt
	 * @param outputType tipo de salida solicitado
	 * @param format     formato de archivo deseado
	 * @param model      modelo de OpenRouter usado
	 */
	public PromptHistory(User user, String promptText, OutputType outputType,
			MediaFormat format, String model) {
		this();
		this.user = user;
		this.promptText = promptText;
		this.outputType = outputType;
		this.format = format;
		this.model = model;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public ProviderResponseLog getResponseLog() {
		return responseLog;
	}

	public void setResponseLog(ProviderResponseLog responseLog) {
		this.responseLog = responseLog;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(completedAt, createdAt, format, id, model, outputType, promptText, responseLog, status,
				user);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptHistory other = (PromptHistory) obj;
		return Objects.equals(completedAt, other.completedAt) && Objects.equals(createdAt, other.createdAt)
				&& format == other.format && Objects.equals(id, other.id) && Objects.equals(model, other.model)
				&& outputType == other.outputType && Objects.equals(promptText, other.promptText)
				&& Objects.equals(responseLog, other.responseLog) && status == other.status
				&& Objects.equals(user, other.user);
	}

	@Override
	public String toString() {
		return "PromptHistory [id=" + id + ", user=" + user + ", promptText=" + promptText + ", outputType="
				+ outputType + ", format=" + format + ", model=" + model + ", status=" + status + ", createdAt="
				+ createdAt + ", completedAt=" + completedAt + ", responseLog=" + responseLog + "]";
	}
	
	
}
