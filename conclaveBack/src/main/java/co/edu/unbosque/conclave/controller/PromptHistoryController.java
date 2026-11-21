package co.edu.unbosque.conclave.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.conclave.dto.PromptHistoryDTO;
import co.edu.unbosque.conclave.model.User;
import co.edu.unbosque.conclave.service.PromptHistoryService;

@RestController
@RequestMapping("/history")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:4200"
})
public class PromptHistoryController {

    private final PromptHistoryService historyService;

    public PromptHistoryController(PromptHistoryService historyService) {
        this.historyService = historyService;
    }

  
    @GetMapping("/me")
    public ResponseEntity<List<PromptHistoryDTO>> obtenerMiHistorial(
            @AuthenticationPrincipal User usuario) {

        List<PromptHistoryDTO> historial =
                historyService.obtenerHistorial(usuario.getUsername());

        return ResponseEntity.ok(historial);
    }
}
