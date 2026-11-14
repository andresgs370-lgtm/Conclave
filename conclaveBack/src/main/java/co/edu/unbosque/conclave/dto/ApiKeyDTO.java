package co.edu.unbosque.conclave.dto;

import co.edu.unbosque.conclave.model.AIProvider;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO de respuesta para las API keys.
 * Nunca expone la clave real; muestra una versión enmascarada.
 */
public class ApiKeyDTO {
	/** Proveedor de IA al que pertenece esta clave. */
	private AIProvider provider;

	/** Versión enmascarada de la clave (p.ej. {@code "sk-****1234"}). */
	private String maskedKey;

	/** Indica si la clave está activa. */
	private boolean active;

	/** Fecha y hora de la última actualización. */
	private LocalDateTime updatedAt;

	/** Nombre del administrador que realizó la última actualización. */
	private String updatedBy;
	
	/** Constructor por defecto. */
	public ApiKeyDTO() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param provider   proveedor de IA
	 * @param maskedKey  clave enmascarada
	 * @param active     estado activo/inactivo
	 * @param updatedAt  fecha de última actualización
	 * @param updatedBy  administrador que la actualizó
	 */
	public ApiKeyDTO(AIProvider provider, String maskedKey, boolean active,
			LocalDateTime updatedAt, String updatedBy) {
		this.provider = provider;
		this.maskedKey = maskedKey;
		this.active = active;
		this.updatedAt = updatedAt;
		this.updatedBy = updatedBy;
	}

	public AIProvider getProvider() {
		return provider;
	}

	public void setProvider(AIProvider provider) {
		this.provider = provider;
	}

	public String getMaskedKey() {
		return maskedKey;
	}

	public void setMaskedKey(String maskedKey) {
		this.maskedKey = maskedKey;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Boolean.valueOf(active), maskedKey, provider, updatedAt, updatedBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiKeyDTO other = (ApiKeyDTO) obj;
		return active == other.active && Objects.equals(maskedKey, other.maskedKey) && provider == other.provider
				&& Objects.equals(updatedAt, other.updatedAt) && Objects.equals(updatedBy, other.updatedBy);
	}

	@Override
	public String toString() {
		return "ApiKeyDTO [provider=" + provider + ", maskedKey=" + maskedKey + ", active=" + active + ", updatedAt="
				+ updatedAt + ", updatedBy=" + updatedBy + "]";
	}
	
	
	
}
