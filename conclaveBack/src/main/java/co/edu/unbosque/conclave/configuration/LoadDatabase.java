package co.edu.unbosque.conclave.configuration;

import co.edu.unbosque.conclave.model.AIProvider;
import co.edu.unbosque.conclave.model.ApiKey;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.repository.ApiKeyRepository;
import co.edu.unbosque.conclave.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Clase de configuración para cargar datos iniciales en la base de datos.
 *
 * <p>Al arrancar la aplicación crea, si no existen:</p>
 * <ul>
 *   <li>El usuario {@code admin} con rol {@code ADMIN}.</li>
 *   <li>El usuario {@code normaluser} con rol {@code USER}.</li>
 *   <li>El registro de {@link ApiKey} para OpenRouter, con
 *       {@code encryptedKey = null} y {@code active = false}, listo para que
 *       el administrador configure la clave desde la API.</li>
 * </ul>
 */
@Configuration
public class LoadDatabase {
	
	/** Logger para registrar mensajes durante la carga de datos. */
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	/**
	 * Inicializa la base de datos con usuarios y el registro de API key de OpenRouter.
	 *
	 * @param userRepo        repositorio de usuarios
	 * @param apiKeyRepo      repositorio de API keys
	 * @param passwordEncoder codificador de contraseñas
	 * @return {@link CommandLineRunner} que se ejecuta al arrancar la aplicación
	 */
	@Bean
	CommandLineRunner initDatabase(UserRepository userRepo,
			ApiKeyRepository apiKeyRepo,
			PasswordEncoder passwordEncoder) {
		return args -> {
			Optional<User> foundAdmin = userRepo.findByUsername("admin");
			if (foundAdmin.isPresent()) {
				log.info("El administrador ya existe, omitiendo la creación del administrador...");
			} else {
				User adminUser = new User("admin",
						passwordEncoder.encode("1234567890"), User.Role.ADMIN);
				adminUser.setEmail("admin@conclave.com");
				userRepo.save(adminUser);
				log.info("Precargando usuario administrador");
			}

			Optional<User> foundNormal = userRepo.findByUsername("normaluser");
			if (foundNormal.isPresent()) {
				log.info("El usuario normal ya existe, omitiendo la creación del usuario normal...");
			} else {
				User normalUser = new User("normaluser",
						passwordEncoder.encode("1234567890"), User.Role.USER);
				normalUser.setEmail("user@conclave.com");
				userRepo.save(normalUser);
				log.info("Precargando usuario normal");
			}
			
            for (AIProvider provider : AIProvider.values()) {
                if (apiKeyRepo.findByProvider(provider).isEmpty()) {
                    apiKeyRepo.save(new ApiKey(provider));
                    log.info("Creando registro vacío de API key para {}", provider.getDisplayName());
                }
            }

		};
	}

}
