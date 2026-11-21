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

@Service
public class PromptHistoryService {

    private final PromptHistoryRepository historyRepository;

    public PromptHistoryService(PromptHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Guarda únicamente el prompt cuando la generación fue exitosa.
     * La fecha de creación se genera automáticamente en el backend.
     */
    @Transactional
    public void guardarPrompt(PromptRequestDTO request, User usuario) {
        if (request == null || usuario == null) {
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
     */
    @Transactional(readOnly = true)
    public List<PromptHistoryDTO> obtenerHistorial(String username) {
        List<PromptHistory> registros = historyRepository.findByUserUsernameOrderByCreatedAtDesc(username);
        return registros.stream()
                .map(this::toDTO)
                .toList();
    }

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