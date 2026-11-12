package co.edu.unbosque.conclave.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad que representa un usuario del sistema.
 * Implementa {@link UserDetails} para integrarse con Spring Security.
 *
 * <p>Esta entidad se almacena en la tabla {@code useraccount} de la base de datos.</p>
 */
@Entity
@Table(name = "useraccount")
public class User implements UserDetails, Serializable {

	/** Número de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Identificador único del usuario. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre de usuario único en el sistema. */
	@Column(unique = true)
	private String username;

	/** Correo electrónico único del usuario. */
	@Column(unique = true)
	private String email;

	/** Nombre completo del usuario (opcional). */
	private String fullName;

	/** Contraseña del usuario (almacenada con encriptación BCrypt). */
	private String password;

	/** Rol del usuario en el sistema. */
	@Enumerated(EnumType.STRING)
	private Role role;

	/** Indica si la cuenta del usuario no ha expirado. */
	private boolean accountNonExpired;

	/** Indica si la cuenta del usuario no está bloqueada. */
	private boolean accountNonLocked;

	/** Indica si las credenciales del usuario no han expirado. */
	private boolean credentialsNonExpired;

	/** Indica si la cuenta del usuario está habilitada. */
	private boolean enabled;

	/** Fecha y hora de creación del usuario. Se asigna automáticamente al persistir. */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Historial de prompts enviados por este usuario.
	 * Relación lazy — no se incluye en {@code toString} para evitar
	 * {@code LazyInitializationException}.
	 
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
			fetch = FetchType.LAZY)
	private List<PromptHistory> history = new ArrayList<>();
*/
	// ── Lifecycle ─────────────────────────────────────────────────────────────

	/**
	 * Asigna la fecha de creación antes de persistir la entidad.
	 */
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// ── Enumeración de roles ──────────────────────────────────────────────────

	/**
	 * Define los roles disponibles en el sistema.
	 *
	 * <ul>
	 *   <li>{@code USER}: usuario regular con permisos básicos.</li>
	 *   <li>{@code ADMIN}: administrador con permisos completos.</li>
	 * </ul>
	 */
	public enum Role {
		/** Usuario regular con permisos básicos. */
		USER,
		/** Administrador con permisos completos. */
		ADMIN
	}

	// ── Constructores ─────────────────────────────────────────────────────────

	/**
	 * Constructor por defecto.
	 * Inicializa todos los flags de cuenta en {@code true} y el rol en {@link Role#USER}.
	 */
	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = true;
		this.role = Role.USER;
	}

	/**
	 * Crea un usuario con nombre de usuario y contraseña.
	 * El rol se establece como {@link Role#USER} por defecto.
	 *
	 * @param username nombre de usuario
	 * @param password contraseña (en texto plano; debe codificarse antes de persistir)
	 */
	public User(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}

	/**
	 * Crea un usuario con nombre de usuario, contraseña y rol explícito.
	 *
	 * @param username nombre de usuario
	 * @param password contraseña (en texto plano; debe codificarse antes de persistir)
	 * @param role     rol asignado al usuario
	 */
	public User(String username, String password, Role role) {
		this();
		this.username = username;
		this.password = password;
		this.role = role;
	}

	/**
	 * Constructor completo con email.
	 * Usado principalmente por {@code UserService} al registrar un usuario nuevo.
	 *
	 * @param username nombre de usuario
	 * @param email    correo electrónico
	 * @param password contraseña (en texto plano; debe codificarse antes de persistir)
	 * @param role     rol asignado al usuario
	 */
	public User(String username, String email, String password, Role role) {
		this();
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	// ── UserDetails ───────────────────────────────────────────────────────────

	/**
	 * {@inheritDoc}
	 * Devuelve la autoridad del usuario con el prefijo {@code ROLE_}.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAccountNonExpired() { return accountNonExpired; }

	/** {@inheritDoc} */
	@Override
	public boolean isAccountNonLocked() { return accountNonLocked; }

	/** {@inheritDoc} */
	@Override
	public boolean isCredentialsNonExpired() { return credentialsNonExpired; }

	/** {@inheritDoc} */
	@Override
	public boolean isEnabled() { return enabled; }

	// ── Getters y Setters ─────────────────────────────────────────────────────

	/** @return identificador único del usuario */
	public Long getId() { return id; }

	/** @param id identificador único a establecer */
	public void setId(Long id) { this.id = id; }

	/** {@inheritDoc} */
	@Override
	public String getUsername() { return username; }

	/** @param username nombre de usuario a establecer */
	public void setUsername(String username) { this.username = username; }

	/** @return correo electrónico del usuario */
	public String getEmail() { return email; }

	/** @param email correo electrónico a establecer */
	public void setEmail(String email) { this.email = email; }

	/** @return nombre completo del usuario */
	public String getFullName() { return fullName; }

	/** @param fullName nombre completo a establecer */
	public void setFullName(String fullName) { this.fullName = fullName; }

	/** {@inheritDoc} */
	@Override
	public String getPassword() { return password; }

	/** @param password contraseña a establecer */
	public void setPassword(String password) { this.password = password; }

	/** @return rol del usuario */
	public Role getRole() { return role; }

	/** @param role rol a establecer */
	public void setRole(Role role) { this.role = role; }

	/** @param accountNonExpired {@code true} si la cuenta no ha expirado */
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	/** @param accountNonLocked {@code true} si la cuenta no está bloqueada */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	/** @param credentialsNonExpired {@code true} si las credenciales no han expirado */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	/** @param enabled {@code true} si la cuenta está habilitada */
	public void setEnabled(boolean enabled) { this.enabled = enabled; }

	/** @return fecha y hora de creación del usuario */
	public LocalDateTime getCreatedAt() { return createdAt; }

	/** @return historial de prompts del usuario 
	public List<PromptHistory> getHistory() { return history; }

	/** @param history historial de prompts a establecer 
	public void setHistory(List<PromptHistory> history) { this.history = history; }

*/
	// ── hashCode, equals, toString ────────────────────────────────────────────

	/**
	 * Calcula el código hash basado en {@code id}, {@code password} y {@code username}.
	 *
	 * @return código hash calculado
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, password, username);
	}

	/**
	 * Dos usuarios son iguales si comparten {@code id}, {@code password} y {@code username}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si los objetos son iguales
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		User other = (User) obj;
		return Objects.equals(id, other.id)
				&& Objects.equals(password, other.password)
				&& Objects.equals(username, other.username);
	}

	/**
	 * Representación de texto del usuario.
	 * <strong>No incluye {@code password} ni {@code history}</strong> para evitar
	 * fugas de información y {@code LazyInitializationException}.
	 *
	 * @return cadena de texto con id, username, email, role y enabled
	 */
	@Override
	public String toString() {
		return "User [id=" + id
				+ ", username=" + username
				+ ", email=" + email
				+ ", role=" + role
				+ ", enabled=" + enabled + "]";
	}
}
