package co.edu.unbosque.conclave.repository;

import co.edu.unbosque.conclave.model.PromptHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link PromptHistory}.
 * Extiende {@link JpaRepository} para heredar las operaciones CRUD básicas.
 */
public interface PromptHistoryRepository extends JpaRepository<PromptHistory, Long>  {
	/**
	 * Devuelve el historial de prompts de un usuario ordenado por fecha descendente.
	 *
	 * @param username nombre de usuario
	 * @return lista de registros de historial del usuario
	 */
	public List<PromptHistory> findByUserUsernameOrderByCreatedAtDesc(String username);

	/**
	 * Devuelve el historial de prompts de un usuario (por ID) ordenado por fecha descendente.
	 *
	 * @param userId identificador del usuario
	 * @return lista de registros de historial del usuario
	 */
	public List<PromptHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

	/**
	 * Devuelve todos los registros de historial ordenados por fecha descendente.
	 *
	 * @return lista completa de historial
	 */
	public List<PromptHistory> findAllByOrderByCreatedAtDesc();
}
