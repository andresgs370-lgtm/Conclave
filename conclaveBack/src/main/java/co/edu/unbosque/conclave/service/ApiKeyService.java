package co.edu.unbosque.conclave.service;

import co.edu.unbosque.conclave.dto.ApiKeyDTO;
import co.edu.unbosque.conclave.dto.ApiKeyUpdateDTO;
import co.edu.unbosque.conclave.exception.ApiKeyNotConfiguredException;
import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.ApiKey;
import co.edu.unbosque.conclave.repository.ApiKeyRepository;
import co.edu.unbosque.conclave.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las API keys de los proveedores de IA.
 *
 * <p>Las claves se almacenan cifradas con AES a través de {@link AESUtil};
 * <strong>nunca</strong> se persisten en texto plano. Las respuestas hacia el
 * cliente muestran únicamente una versión enmascarada.</p>
 */
@Service
public class ApiKeyService {

	/** Repositorio para acceder a los registros de API keys en la base de datos. */
	@Autowired
	private ApiKeyRepository apiKeyRepo;

	/** Constructor por defecto. */
	public ApiKeyService() {
	}

	/**
	 * Devuelve todas las API keys como DTOs con la clave enmascarada.
	 *
	 * @return lista de {@link ApiKeyDTO} con información de cada proveedor
	 */
	public List<ApiKeyDTO> getAll() {
		List<ApiKey> entities = apiKeyRepo.findAll();
		List<ApiKeyDTO> dtos = new ArrayList<>();
		for (ApiKey entity : entities) {
			dtos.add(toDTO(entity));
		}
		return dtos;
	}
	
	/**
	 * Actualiza la API key y/o el estado activo de un proveedor.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: actualización exitosa.</li>
	 *   <li>Devuelve {@code 1}: proveedor no encontrado en la base de datos.</li>
	 * </ul>
	 *
	 * @param provider      proveedor cuya key se va a actualizar
	 * @param data          DTO con la nueva clave en texto plano y/o el nuevo estado
	 * @param adminUsername nombre del administrador que realiza el cambio
	 * @return código de resultado
	 */
	public int updateKey(AIProvider provider, ApiKeyUpdateDTO data, String adminUsername) {
		Optional<ApiKey> found = apiKeyRepo.findByProvider(provider);
		if (found.isEmpty()) {
			return 1;
		}

		ApiKey entity = found.get();

		if (data.getNewKey() != null && !data.getNewKey().isBlank()) {
			entity.setEncryptedKey(AESUtil.encrypt(data.getNewKey()));
		}

		if (data.getActive() != null) {
			entity.setActive(data.getActive());
		}

		entity.setUpdatedAt(LocalDateTime.now());
		entity.setUpdatedBy(adminUsername);
		apiKeyRepo.save(entity);
		return 0;
	}

	/**
	 * Cambia el estado activo/inactivo de la API key de un proveedor.
	 *
	 * <ul>
	 *   <li>Devuelve {@code 0}: estado actualizado.</li>
	 *   <li>Devuelve {@code 1}: proveedor no encontrado.</li>
	 * </ul>
	 *
	 * @param provider proveedor a activar o desactivar
	 * @param active   {@code true} para activar, {@code false} para desactivar
	 * @return código de resultado
	 */
	public int setActive(AIProvider provider, boolean active) {
		Optional<ApiKey> found = apiKeyRepo.findByProvider(provider);
		if (found.isEmpty()) {
			return 1;
		}
		ApiKey entity = found.get();
		entity.setActive(active);
		apiKeyRepo.save(entity);
		return 0;
	}
	/**
	 * Devuelve la clave descifrada de un proveedor para usarla en llamadas a la IA.
	 *
	 * @param provider proveedor del que se quiere la clave
	 * @return clave en texto plano
	 * @throws ApiKeyNotConfiguredException si la clave no existe o está vacía
	 */
	public String getDecryptedKey(AIProvider provider) throws ApiKeyNotConfiguredException {
		Optional<ApiKey> found = apiKeyRepo.findByProvider(provider);
		if (found.isEmpty()) {
			throw new ApiKeyNotConfiguredException(provider);
		}
		ApiKey entity = found.get();
		if (entity.getEncryptedKey() == null || entity.getEncryptedKey().isBlank()) {
			throw new ApiKeyNotConfiguredException(provider);
		}
		return AESUtil.decrypt(entity.getEncryptedKey());
	}

	/**
	 * Indica si un proveedor tiene su API key configurada y activa.
	 *
	 * @param provider proveedor a verificar
	 * @return {@code true} si la clave existe, no está vacía y está activa
	 */
	public boolean isConfigured(AIProvider provider) {
		Optional<ApiKey> found = apiKeyRepo.findByProvider(provider);
		if (found.isEmpty()) {
			return false;
		}
		ApiKey entity = found.get();
		return entity.getEncryptedKey() != null
				&& !entity.getEncryptedKey().isBlank()
				&& entity.isActive();
	}
	
	/**
	 * Convierte una entidad {@link ApiKey} en su DTO enmascarado.
	 * La clave visible nunca supera 8 caracteres del texto plano.
	 *
	 * @param entity entidad a convertir
	 * @return DTO con la clave enmascarada
	 */
	private ApiKeyDTO toDTO(ApiKey entity) {
		String masked = maskKey(entity.getEncryptedKey());
		return new ApiKeyDTO(
				entity.getProvider(),
				masked,
				entity.isActive(),
				entity.getUpdatedAt(),
				entity.getUpdatedBy());
	}

	/**
	 * Enmascara una clave cifrada para mostrársela al administrador.
	 * Si la clave es nula o está vacía devuelve {@code "sin configurar"}.
	 *
	 * @param encryptedKey clave cifrada (Base64)
	 * @return cadena con formato {@code "****...XXXX"} o {@code "sin configurar"}
	 */
	private String maskKey(String encryptedKey) {
		if (encryptedKey == null || encryptedKey.isBlank()) {
			return "sin configurar";
		}
		// Muestra solo los últimos 4 caracteres del texto cifrado
		int len = encryptedKey.length();
		String suffix = encryptedKey.substring(Math.max(0, len - 4));
		return "****" + suffix;
	}

}
