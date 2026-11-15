package co.edu.unbosque.conclave.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuración del pool de hilos para las llamadas asíncronas a los proveedores de IA.
 *
 * <p>Expone un bean {@code Executor} llamado {@code aiExecutor} que será inyectado
 * por {@code PromptService} para lanzar las peticiones a cada proveedor en paralelo.
 * Usa un virtual-thread executor (Java 21) para máxima eficiencia en tareas I/O-bound.</p>
 */
@Configuration
public class AsyncConfig {
	/**
	 * Crea el executor para las llamadas a los proveedores de IA.
	 *
	 * <p>Se usa {@link Executors#newVirtualThreadPerTaskExecutor()} de Java 21,
	 * que crea un hilo virtual por tarea y es ideal para operaciones I/O-bound
	 * como las llamadas HTTP a las APIs de IA.</p>
	 *
	 * @return executor de hilos virtuales
	 */
	@Bean
	public Executor aiExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

}
