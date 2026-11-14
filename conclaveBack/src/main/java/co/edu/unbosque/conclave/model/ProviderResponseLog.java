package co.edu.unbosque.conclave.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Entidad que registra la respuesta de OpenRouter para un {@link PromptHistory} específico.
 *
 * <p>Con un único proveedor, la relación es 1:1 con {@link PromptHistory}.
 * Los bytes de contenido multimedia <strong>nunca</strong> se persisten aquí;
 * solo se almacenan metadatos (estado, modelo, duración, texto o error).</p>
 */
@Entity
@Table(name = "provider_response_logs")
public class ProviderResponseLog implements Serializable{

	/** Número de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Identificador único del log. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Registro de historial al que pertenece este log (relación 1:1). */
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "history_id", nullable = false, unique = true)
	private PromptHistory history;

	/** Proveedor de IA — siempre {@link AIProvider#OPENROUTER}. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AIProvider provider = AIProvider.OPENROUTER;

	/** Nombre del modelo concreto utilizado. */
	@Column(length = 100)
	private String model;

	/** Estado de la respuesta. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private ResponseStatus status;

	/** Código HTTP devuelto por OpenRouter. */
	@Column(nullable = false)
	private int httpStatus;

	/** Mensaje de error cuando {@code status != SUCCESS}. */
	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	/** Duración total de la llamada a OpenRouter en milisegundos. */
	@Column(nullable = false)
	private long durationMs;

	/** Constructor por defecto. */
	public ProviderResponseLog() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param history      historial al que pertenece este log
	 * @param model        nombre del modelo usado
	 * @param status       estado de la respuesta
	 * @param httpStatus   código HTTP de OpenRouter
	 * @param textContent  texto generado (puede ser {@code null})
	 * @param errorMessage mensaje de error (puede ser {@code null})
	 * @param durationMs   duración de la llamada en ms
	 */
	public ProviderResponseLog(PromptHistory history, String model,
			ResponseStatus status, int httpStatus,
			String textContent, String errorMessage, long durationMs) {
		this();
		this.history = history;
		this.model = model;
		this.status = status;
		this.httpStatus = httpStatus;
		this.errorMessage = errorMessage;
		this.durationMs = durationMs;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PromptHistory getHistory() {
		return history;
	}

	public void setHistory(PromptHistory history) {
		this.history = history;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(durationMs), errorMessage, history, Integer.valueOf(httpStatus), id, model,
				provider, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProviderResponseLog other = (ProviderResponseLog) obj;
		return durationMs == other.durationMs && Objects.equals(errorMessage, other.errorMessage)
				&& Objects.equals(history, other.history) && httpStatus == other.httpStatus
				&& Objects.equals(id, other.id) && Objects.equals(model, other.model) && provider == other.provider
				&& status == other.status;
	}

	@Override
	public String toString() {
		return "ProviderResponseLog [id=" + id + ", history=" + history + ", provider=" + provider + ", model=" + model
				+ ", status=" + status + ", httpStatus=" + httpStatus + ", errorMessage=" + errorMessage
				+ ", durationMs=" + durationMs + "]";
	}
	
	
}
