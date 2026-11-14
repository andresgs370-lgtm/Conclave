package co.edu.unbosque.conclave.dto;

import java.util.Objects;

/**
 * Respuesta del endpoint de autenticación.
 * Devuelve el JWT junto con los datos básicos del usuario autenticado.
 */
public class AuthResponseDTO {
	/** Token JWT generado para el usuario autenticado. */
	private String token;

	/** Nombre de usuario autenticado. */
	private String username;

	/** Rol del usuario autenticado (p.ej. {@code "ADMIN"} o {@code "USER"}). */
	private String role;

	/** Constructor por defecto. */
	public AuthResponseDTO() {
	}

	/**
	 * Constructor completo.
	 *
	 * @param token    token JWT
	 * @param username nombre de usuario
	 * @param role     rol del usuario
	 */
	public AuthResponseDTO(String token, String username, String role) {
		this.token = token;
		this.username = username;
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(role, token, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthResponseDTO other = (AuthResponseDTO) obj;
		return Objects.equals(role, other.role) && Objects.equals(token, other.token)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "AuthResponseDTO [token=" + token + ", username=" + username + ", role=" + role + "]";
	}
	
}
