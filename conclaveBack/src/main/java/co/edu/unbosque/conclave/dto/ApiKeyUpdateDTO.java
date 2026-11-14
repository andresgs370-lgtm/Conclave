package co.edu.unbosque.conclave.dto;

import java.util.Objects;

/**
 * DTO de entrada para actualizar una API key.
 * Ambos campos son opcionales: se puede cambiar solo la clave, solo el estado, o ambos.
 */
public class ApiKeyUpdateDTO {
	/**
	 * Nueva clave en texto plano.
	 * El servicio la cifrará con AES antes de persistirla.
	 */
	private String newKey;

	/**
	 * Si se envía, establece el estado activo/inactivo de la clave.
	 * Es {@link Boolean} para distinguir "no enviado" de {@code false}.
	 */
	private Boolean active;
	
	/** Constructor por defecto. */
	public ApiKeyUpdateDTO() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param newKey nueva clave en texto plano
	 * @param active nuevo estado
	 */
	public ApiKeyUpdateDTO(String newKey, Boolean active) {
		this.newKey = newKey;
		this.active = active;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, newKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiKeyUpdateDTO other = (ApiKeyUpdateDTO) obj;
		return Objects.equals(active, other.active) && Objects.equals(newKey, other.newKey);
	}

	@Override
	public String toString() {
		return "ApiKeyUpdateDTO [newKey=" + newKey + ", active=" + active + "]";
	}
	
	
}
