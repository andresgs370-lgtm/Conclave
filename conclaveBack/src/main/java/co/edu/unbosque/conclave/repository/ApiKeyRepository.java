package co.edu.unbosque.conclave.repository;

import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ApiKey}.
 * Extiende {@link JpaRepository} para heredar las operaciones CRUD básicas
 * (save, findById, findAll, deleteById, count, existsById, etc.).
 */
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long>{
	/**
	 * Busca la API key de un proveedor específico.
	 *
	 * @param provider proveedor de IA
	 * @return {@code Optional} con la API key si existe, vacío si no
	 */
	public Optional<ApiKey> findByProvider(AIProvider provider);

	/**
	 * Devuelve todas las API keys que están actualmente activas.
	 *
	 * @return lista de API keys activas
	 */
	public List<ApiKey> findByActiveTrue();

	/**
	 * Devuelve todas las API keys que están actualmente inactivas.
	 *
	 * @return lista de API keys inactivas
	 */
	public List<ApiKey> findByActiveFalse();

	/**
	 * Verifica si existe un registro de API key para el proveedor indicado.
	 *
	 * @param provider proveedor de IA
	 * @return {@code true} si ya existe un registro para ese proveedor
	 */
	public boolean existsByProvider(AIProvider provider);
}
