package co.edu.unbosque.conclave.repository;

import co.edu.unbosque.conclave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 * Extiende {@link JpaRepository} para heredar las operaciones CRUD básicas
 * (save, findById, findAll, deleteById, count, existsById, etc.).
 */
public interface UserRepository extends JpaRepository<User, Long>{
	/**
	 * Busca un usuario por su nombre de usuario.
	 *
	 * @param username nombre de usuario a buscar
	 * @return {@code Optional} con el usuario si existe, vacío si no
	 */
	public Optional<User> findByUsername(String username);

	/**
	 * Verifica si existe un usuario con el correo electrónico indicado.
	 *
	 * @param email correo electrónico a verificar
	 * @return {@code true} si existe al menos un usuario con ese email
	 */
	public boolean existsByEmail(String email);

	/**
	 * Elimina el usuario con el nombre de usuario indicado.
	 *
	 * @param username nombre de usuario del usuario a eliminar
	 */
	public void deleteByUsername(String username);
}
