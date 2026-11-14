package co.edu.unbosque.conclave.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que almacena la API key cifrada de cada proveedor de IA.
 * <p>La clave se guarda cifrada con AES a través de {@code AESUtil};
 * <strong>nunca</strong> en texto plano. {@code encryptedKey} puede ser
 * {@code null} cuando el administrador aún no ha configurado un proveedor</p>
 */
@Entity
@Table(name = "api_keys")
public class ApiKey implements Serializable {
	/** Número de versión para la serialización */
	private static final long serialVersionUID = 1L;

	/** Identificador único de la API key */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Proveedor de IA al que pertenece esta clave. Valor único en la tabla. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 20)
	private AIProvider provider;

	/**
	 * Clave cifrada con AES. Puede ser {@code null} si el proveedor aún no está configurado.
	 * Usar {@code AESUtil.encrypt} / {@code decrypt} para manipularla.
	 */
	@Column(length = 1000)
	private String encryptedKey;

	/** Indica si la clave está activa y debe usarse en las llamadas a la IA. */
	@Column(nullable = false)
	private boolean active = false;

	/** Fecha y hora de la última actualización de la clave. */
	@Column
	private LocalDateTime updatedAt;

	/** Nombre del administrador que realizó la última actualización. */
	@Column(length = 50)
	private String updatedBy;

	/** Constructor por defecto. */
	public ApiKey() {
	}

	/**
	 * Crea una API key con proveedor y sin clave configurada (estado inicial).
	 *
	 * @param provider proveedor de IA
	 */
	public ApiKey(AIProvider provider) {
		this();
		this.provider = provider;
	}

	/**
	 * Crea una API key completamente inicializada.
	 *
	 * @param provider     proveedor de IA
	 * @param encryptedKey clave cifrada con AES
	 * @param updatedBy    nombre del administrador que la configuró
	 */
	public ApiKey(AIProvider provider, String encryptedKey, String updatedBy) {
		this();
		this.provider = provider;
		this.encryptedKey = encryptedKey;
		this.updatedBy = updatedBy;
		this.updatedAt = LocalDateTime.now();
	}
	
	/** @return identificador único */
	public Long getId() { return id; }

	/** @param id identificador a establecer */
	public void setId(Long id) { this.id = id; }

	/** @return proveedor de IA */
	public AIProvider getProvider() { return provider; }

	/** @param provider proveedor de IA a establecer */
	public void setProvider(AIProvider provider) { this.provider = provider; }

	/** @return clave cifrada (puede ser {@code null}) */
	public String getEncryptedKey() { return encryptedKey; }

	/** @param encryptedKey clave cifrada a establecer */
	public void setEncryptedKey(String encryptedKey) { this.encryptedKey = encryptedKey; }

	/** @return {@code true} si la clave está activa */
	public boolean isActive() { return active; }

	/** @param active {@code true} para activar la clave */
	public void setActive(boolean active) { this.active = active; }

	/** @return fecha y hora de la última actualización */
	public LocalDateTime getUpdatedAt() { return updatedAt; }

	/** @param updatedAt fecha y hora de actualización a establecer */
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	/** @return nombre del administrador que actualizó la clave */
	public String getUpdatedBy() { return updatedBy; }

	/** @param updatedBy nombre del administrador a establecer */
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy;}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, provider);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ApiKey other = (ApiKey) obj;
		return Objects.equals(id, other.id) && provider == other.provider;
	}

	@Override
	public String toString() {
		return "ApiKey [id=" + id + ", provider=" + provider + ", active=" + active
				+ ", updatedBy=" + updatedBy + ", updatedAt=" + updatedAt + "]";
	}
}
