package co.edu.unbosque.conclave.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.conclave.dto.PromptHistoryDTO;
import co.edu.unbosque.conclave.service.PromptHistoryService;

/**
 * Controlador REST para consultar el historial de prompts del usuario autenticado.
 */
@RestController
@RequestMapping("/history")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:4200"
})
public class PromptHistoryController {

    private final PromptHistoryService historyService;

    /**
     * Crea una instancia de {@code PromptHistoryController} con su servicio asociado.
     *
     * @param historyService servicio encargado de consultar el historial de prompts
     */
    public PromptHistoryController(PromptHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Obtiene el historial de prompts del usuario autenticado actualmente.
     *
     * @param usuario detalles del usuario autenticado mediante JWT
     * @return respuesta HTTP con la lista de {@link PromptHistoryDTO} del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<List<PromptHistoryDTO>> obtenerMiHistorial(
            @AuthenticationPrincipal UserDetails usuario) {

        if (usuario == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<PromptHistoryDTO> historial =
                historyService.obtenerHistorial(usuario.getUsername());

        return ResponseEntity.ok(historial);
    }
}