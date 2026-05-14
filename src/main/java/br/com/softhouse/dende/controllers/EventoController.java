package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.services.EventoService;

import java.util.List;

@Controller
@RequestMapping(path = "/organizadores")
public class EventoController {

    private final EventoService eventoService;

    public EventoController() {
        this.eventoService = new EventoService();
    }

    @PostMapping(path = "/{organizadorId}/eventos")
    public ResponseEntity<Object> cadastrarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @RequestBody EventoRequestDTO dto) {
        try {
            EventoResponseDTO response = eventoService.cadastrar(organizadorId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PutMapping(path = "/{organizadorId}/eventos/{eventoId}")
    public ResponseEntity<Object> alterarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody EventoRequestDTO dto) {
        try {
            EventoResponseDTO response = eventoService.alterar(organizadorId, eventoId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PatchMapping(path = "/{organizadorId}/eventos/{eventoId}/{status}")
    public ResponseEntity<Object> alterarStatusEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @PathVariable(parameter = "status") String status) {
        try {
            eventoService.alterarStatus(organizadorId, eventoId, status);
            return ResponseEntity.ok("Status do evento atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @GetMapping(path = "/{organizadorId}/eventos")
    public ResponseEntity<Object> listarEventosDoOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId) {
        try {
            List<EventoResponseDTO> response = eventoService.listarDoOrganizador(organizadorId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }
}