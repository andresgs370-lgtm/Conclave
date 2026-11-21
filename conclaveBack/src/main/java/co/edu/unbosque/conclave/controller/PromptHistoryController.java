package co.edu.unbosque.conclave.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.conclave.dto.PromptHistoryDTO;
import co.edu.unbosque.conclave.service.PromptHistoryService;

/**
 * Controlador REST para consultar el historial de prompts.
 *
 * <p>{@code /history/me} lo usa cualquier usuario autenticado; {@code /history} y
 * {@code /history/user/{id}} están restringidos a administradores en la configuración
 * de seguridad.</p>
 */
@RestController
@RequestMapping("/history")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:4200",
        "https://conclaveia.netlify.app"
        
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
     * @param autenticacion autenticación del usuario mediante JWT
     * @return respuesta HTTP con la lista de {@link PromptHistoryDTO} del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<List<PromptHistoryDTO>> obtenerMiHistorial(Authentication autenticacion) {

        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<PromptHistoryDTO> historial =
                historyService.obtenerHistorial(autenticacion.getName());

        return ResponseEntity.ok(historial);
    }

    /**
     * Obtiene el historial de prompts de todos los usuarios. Solo para administradores.
     *
     * @return respuesta HTTP con la lista de {@link PromptHistoryDTO} de todos los usuarios
     */
    @GetMapping
    public ResponseEntity<List<PromptHistoryDTO>> obtenerHistorialCompleto() {
        return ResponseEntity.ok(historyService.obtenerHistorialCompleto());
    }

    /**
     * Obtiene el historial de prompts de un usuario específico. Solo para administradores.
     *
     * @param id identificador del usuario
     * @return respuesta HTTP con la lista de {@link PromptHistoryDTO} del usuario
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<List<PromptHistoryDTO>> obtenerHistorialPorUsuario(@PathVariable("id") Long id) {
        return ResponseEntity.ok(historyService.obtenerHistorialPorUsuario(id));
    }
}