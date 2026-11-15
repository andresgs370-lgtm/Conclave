package co.edu.unbosque.conclave.exception;

import co.edu.unbosque.conclave.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Convierte las excepciones de dominio en respuestas HTTP estandarizadas
 * usando {@link ErrorResponseDTO}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Maneja errores de proveedores de IA (HTTP 500).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 500
	 */
	@ExceptionHandler(AIProviderException.class)
	public ResponseEntity<ErrorResponseDTO> handleAIProvider(
			AIProviderException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDTO(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"Error del proveedor de IA",
						ex.getMessage(),
						request.getRequestURI()));
	}

	/**
	 * Maneja la falta de configuración de una API key (HTTP 503).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 503
	 */
	@ExceptionHandler(ApiKeyNotConfiguredException.class)
	public ResponseEntity<ErrorResponseDTO> handleApiKeyNotConfigured(
			ApiKeyNotConfiguredException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(new ErrorResponseDTO(
						HttpStatus.SERVICE_UNAVAILABLE.value(),
						"Servicio no disponible",
						ex.getMessage(),
						request.getRequestURI()));
	}

	/**
	 * Maneja jobs no encontrados (HTTP 404).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 404
	 */
	@ExceptionHandler(JobNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleJobNotFound(
			JobNotFoundException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponseDTO(
						HttpStatus.NOT_FOUND.value(),
						"No encontrado",
						ex.getMessage(),
						request.getRequestURI()));
	}

	/**
	 * Maneja contenido multimedia expirado (HTTP 410 Gone).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 410
	 */
	@ExceptionHandler(MediaExpiredException.class)
	public ResponseEntity<ErrorResponseDTO> handleMediaExpired(
			MediaExpiredException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.GONE)
				.body(new ErrorResponseDTO(
						HttpStatus.GONE.value(),
						"Contenido expirado",
						ex.getMessage(),
						request.getRequestURI()));
	}

	/**
	 * Maneja tipos de salida no soportados y contraseñas inválidas (HTTP 400).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 400
	 */
	@ExceptionHandler({UnsupportedOutputTypeException.class, PasswordNotValidException.class})
	public ResponseEntity<ErrorResponseDTO> handleBadRequest(
			Exception ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDTO(
						HttpStatus.BAD_REQUEST.value(),
						"Solicitud inválida",
						ex.getMessage(),
						request.getRequestURI()));
	}

	/**
	 * Maneja cualquier excepción no controlada (HTTP 500).
	 *
	 * @param ex      excepción lanzada
	 * @param request petición HTTP original
	 * @return respuesta con estado 500
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGeneric(
			Exception ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDTO(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"Error interno del servidor",
						ex.getMessage(),
						request.getRequestURI()));
	}

}
