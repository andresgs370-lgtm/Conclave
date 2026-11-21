package co.edu.unbosque.conclave.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.conclave.dto.PromptHistoryDTO;
import co.edu.unbosque.conclave.dto.PromptRequestDTO;
import co.edu.unbosque.conclave.model.JobStatus;
import co.edu.unbosque.conclave.model.PromptHistory;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.repository.PromptHistoryRepository;
import co.edu.unbosque.conclave.repository.UserRepository;

/**
 * Servicio encargado de gestionar el guardado y consulta del historial de prompts de los usuarios.
 */
@Service
public class PromptHistoryService {

    private final PromptHistoryRepository historyRepository;
    private final UserRepository userRepository;

    /**
     * Inicializa el servicio con los repositorios de historial y usuarios.
     *
     * @param historyRepository repositorio JPA para registros de historial de prompts
     * @param userRepository repositorio JPA para consultar usuarios
     */
    public PromptHistoryService(PromptHistoryRepository historyRepository,
                                UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Guarda únicamente el prompt cuando la generación fue exitosa.
     * La fecha de creación se genera automáticamente en el backend.
     *
     * @param request datos del prompt generado
     * @param username nombre de usuario autenticado
     */
    @Transactional
    public void guardarPrompt(PromptRequestDTO request, String username) {
        if (request == null || username == null || username.isBlank()) {
            return;
        }

        User usuario = userRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return;
        }

        String iaUtilizada = resolverIaUtilizada(request);

        PromptHistory history = new PromptHistory(
                usuario,
                request.getPromptText(),
                request.getOutputType(),
                request.getFormat(),
                iaUtilizada
        );
        history.setStatus(JobStatus.COMPLETED);
        history.setCompletedAt(LocalDateTime.now());

        historyRepository.save(history);
    }

    /**
     * Obtiene únicamente el historial de prompts del usuario autenticado,
     * ordenado de más reciente a más antiguo.
     *
     * @param username nombre de usuario
     * @return lista de {@link PromptHistoryDTO} del usuario
     */
    @Transactional(readOnly = true)
    public List<PromptHistoryDTO> obtenerHistorial(String username) {
        List<PromptHistory> registros = historyRepository.findByUserUsernameOrderByCreatedAtDesc(username);
        return registros.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Determina el identificador del proveedor o modelo de IA utilizado según el tipo de salida.
     *
     * @param request solicitud con el tipo de salida y parámetros opcionales de proveedor/modelo
     * @return nombre del proveedor o modelo resuelto, o {@code null} si no hay tipo de salida
     */
    private String resolverIaUtilizada(PromptRequestDTO request) {
        if (request.getOutputType() == null) {
            return null;
        }
        return switch (request.getOutputType()) {
            case IMAGE -> (request.getImageProvider() != null && !request.getImageProvider().isBlank())
                    ? request.getImageProvider().trim()
                    : "CLOUDFLARE";
            case AUDIO -> "GEMINI";
            case VIDEO -> {
                if (request.getVideoModel() != null && !request.getVideoModel().isBlank()) {
                    yield request.getVideoModel().trim();
                }
                if (request.getVideoProvider() != null && !request.getVideoProvider().isBlank()) {
                    yield request.getVideoProvider().trim();
                }
                yield "AGNES_AI";
            }
            default -> request.getOutputType().name();
        };
    }

    /**
     * Convierte una entidad {@link PromptHistory} en su representación {@link PromptHistoryDTO}.
     *
     * @param entity entidad de historial a transformar
     * @return DTO con los datos del registro de historial
     */
    private PromptHistoryDTO toDTO(PromptHistory entity) {
        PromptHistoryDTO dto = new PromptHistoryDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUser() != null ? entity.getUser().getUsername() : null);
        dto.setPromptText(entity.getPromptText());
        dto.setOutputType(entity.getOutputType());
        dto.setFormat(entity.getFormat());
        dto.setModel(entity.getModel());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}