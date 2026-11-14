package co.edu.unbosque.conclave.dto;


import co.edu.unbosque.conclave.model.User.Role;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) para usuarios. Utilizada para transferir datos de
 * usuario entre capas de la aplicación, especialmente en operaciones de API.
 */

public class UserDTO {

	/** Identificador único del usuario. */
	private Long id;

	/** Nombre de usuario para autenticación. */
	private String username;

	/** Correo electrónico del usuario. */
	private String email;

	/** Nombre completo del usuario. */
	private String fullName;

	/**
	 * Contraseña del usuario.
	 * Solo se lee del JSON de entrada; <strong>nunca</strong> se incluye en respuestas.
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	/** Rol del usuario en el sistema. */
	private Role role;

	/**
	 * Indica si la cuenta está habilitada. Es {@link Boolean} (nullable) para que
	 * un update que no envíe este campo no deshabilite al usuario.
	 */
	private Boolean enabled;

	/** Fecha y hora de creación del usuario. */
	private LocalDateTime createdAt;

	/** Constructor por defecto. */
	public UserDTO() {
	}

	/**
	 * Constructor con nombre de usuario y contraseña.
	 *
	 * @param username nombre de usuario
	 * @param password contraseña del usuario
	 */
	public UserDTO(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Constructor con nombre de usuario, contraseña y rol.
	 *
	 * @param username nombre de usuario
	 * @param password contraseña del usuario
	 * @param role     rol del usuario
	 */
	public UserDTO(String username, String password, Role role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	/**
	 * Constructor completo para respuestas del servicio.
	 *
	 * @param id        identificador único
	 * @param username  nombre de usuario
	 * @param email     correo electrónico
	 * @param fullName  nombre completo
	 * @param role      rol del usuario
	 * @param enabled   estado de la cuenta
	 * @param createdAt fecha de creación
	 */
	public UserDTO(Long id, String username, String email, String fullName,
			Role role, Boolean enabled, LocalDateTime createdAt) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.fullName = fullName;
		this.role = role;
		this.enabled = enabled;
		this.createdAt = createdAt;
	}
	/**
	 * Obtiene el identificador del usuario.
	 *
	 * @return Identificador único del usuario
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador del usuario.
	 *
	 * @param id Identificador único a establecer
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el nombre de usuario.
	 *
	 * @return Nombre de usuario
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Establece el nombre de usuario.
	 *
	 * @param username Nombre de usuario a establecer
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Obtiene la contraseña del usuario.
	 *
	 * @return Contraseña del usuario
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece la contraseña del usuario.
	 *
	 * @param password Contraseña a establecer
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Obtiene el rol del usuario.
	 *
	 * @return Rol del usuario
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Establece el rol del usuario.
	 *
	 * @param role Rol a establecer
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Calcula el código hash para este objeto. Utilizado para almacenar objetos en colecciones
	 * basadas en hash.
	 *
	 * @return Código hash calculado
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, password, role, username);
	}

	/**
	 * Compara este objeto con otro para determinar igualdad. Dos objetos UserDTO son iguales si
	 * tienen el mismo id, username, password y role.
	 *
	 * @param obj Objeto a comparar
	 * @return true si los objetos son iguales, false en caso contrario
	 */	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    UserDTO other = (UserDTO) obj;
	    return Objects.equals(id, other.id)
	        && Objects.equals(password, other.password)
	        && role == other.role
	        && Objects.equals(username, other.username);
	}
	/**
	 * Devuelve una representación en cadena de texto de este objeto. Útil para depuración y registro.
	 *
	 * @return Representación en cadena de texto del objeto
	 */
	@Override
	public String toString() {
		return "UserDTO [id=" + id + ", username=" + username + ", email=" + email + ", fullName=" + fullName
				+ ", password=" + password + ", role=" + role + ", enabled=" + enabled + ", createdAt=" + createdAt
				+ ", getId()=" + getId() + ", getUsername()=" + getUsername() + ", getPassword()=" + getPassword()
				+ ", getRole()=" + getRole() + ", hashCode()=" + hashCode() + ", getClass()=" + getClass()
				+ ", toString()=" + super.toString() + "]";
	}
}
