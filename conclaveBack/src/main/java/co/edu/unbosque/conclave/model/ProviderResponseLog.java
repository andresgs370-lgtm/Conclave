package co.edu.unbosque.conclave.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entidad que registra la respuesta de un proveedor de IA para un
 * {@link PromptHistory} especifico.
 *
 * 
 * La relacion es 1:1 con {@link PromptHistory}. Los bytes de contenido
 * multimedia nunca se persisten aqui; solo se almacenan
 * metadatos (estado, modelo, duracion y error si aplica).
 * 
 */
@Entity
@Table(name = "provider_response_logs")
public class ProviderResponseLog implements Serializable {

	/** Numero de version para la serializacion. */
	private static final long serialVersionUID = 1L;

	/** Identificador unico del log. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Registro de historial al que pertenece este log (relacion 1:1). */
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "history_id", nullable = false, unique = true)
	private PromptHistory history;

	/** Proveedor de IA que genero la respuesta. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AIProvider provider;

	/** Nombre del modelo concreto utilizado. */
	@Column(length = 100)
	private String model;

	/** Estado de la respuesta. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private ResponseStatus status;

	/** Codigo HTTP devuelto por el proveedor. */
	@Column(nullable = false)
	private int httpStatus;

	/** Mensaje de error cuando {@code status != SUCCESS}. */
	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	/** Duracion total de la llamada al proveedor en milisegundos. */
	@Column(nullable = false)
	private long durationMs;

	/** Constructor por defecto. */
	public ProviderResponseLog() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param history      historial al que pertenece este log
	 * @param provider     proveedor de IA que genero la respuesta
	 * @param model        nombre del modelo usado
	 * @param status       estado de la respuesta
	 * @param httpStatus   codigo HTTP del proveedor
	 * @param errorMessage mensaje de error (puede ser {@code null})
	 * @param durationMs   duracion de la llamada en ms
	 */
	public ProviderResponseLog(PromptHistory history, AIProvider provider, String model, ResponseStatus status,
			int httpStatus, String errorMessage, long durationMs) {
		this();
		this.history = history;
		this.provider = provider;
		this.model = model;
		this.status = status;
		this.httpStatus = httpStatus;
		this.errorMessage = errorMessage;
		this.durationMs = durationMs;
	}

	/** @return identificador unico del log */
	public Long getId() {
		return id;
	}

	/** @param id identificador a establecer */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return historial asociado */
	public PromptHistory getHistory() {
		return history;
	}

	/** @param history historial a establecer */
	public void setHistory(PromptHistory history) {
		this.history = history;
	}

	/** @return proveedor de IA */
	public AIProvider getProvider() {
		return provider;
	}

	/** @param provider proveedor a establecer */
	public void setProvider(AIProvider provider) {
		this.provider = provider;
	}

	/** @return nombre del modelo utilizado */
	public String getModel() {
		return model;
	}

	/** @param model modelo a establecer */
	public void setModel(String model) {
		this.model = model;
	}

	/** @return estado de la respuesta */
	public ResponseStatus getStatus() {
		return status;
	}

	/** @param status estado a establecer */
	public void setStatus(ResponseStatus status) {
		this.status = status;
	}

	/** @return codigo HTTP devuelto por el proveedor */
	public int getHttpStatus() {
		return httpStatus;
	}

	/** @param httpStatus codigo HTTP a establecer */
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	/** @return mensaje de error o {@code null} si no hubo error */
	public String getErrorMessage() {
		return errorMessage;
	}

	/** @param errorMessage mensaje de error a establecer */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/** @return duracion de la llamada en milisegundos */
	public long getDurationMs() {
		return durationMs;
	}

	/** @param durationMs duracion a establecer */
	public void setDurationMs(long durationMs) {
		this.durationMs = durationMs;
	}

	/**
	 * @return numero de version para la serializacion
	 */
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
