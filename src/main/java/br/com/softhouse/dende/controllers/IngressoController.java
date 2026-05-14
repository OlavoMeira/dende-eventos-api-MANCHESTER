package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.IngressoRequestDTO;
import br.com.softhouse.dende.dto.response.IngressoResponseDTO;
import br.com.softhouse.dende.services.IngressoService;

import java.util.List;

@Controller
public class IngressoController {

    private final IngressoService ingressoService;

    public IngressoController() {
        this.ingressoService = new IngressoService();
    }

    @PostMapping(path = "/organizadores/{organizadorId}/eventos/{eventoId}/ingressos")
    public ResponseEntity<Object> comprarIngresso(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody IngressoRequestDTO dto) {
        try {
            IngressoResponseDTO response = ingressoService.comprar(organizadorId, eventoId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PostMapping(path = "/usuarios/{usuarioId}/ingressos/{ingressoId}")
    public ResponseEntity<Object> cancelarIngresso(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @PathVariable(parameter = "ingressoId") long ingressoId) {
        try {
            IngressoResponseDTO response = ingressoService.cancelar(usuarioId, ingressoId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @GetMapping(path = "/usuarios/{usuarioId}/ingressos")
    public ResponseEntity<Object> listarIngressosDoUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId) {
        try {
            List<IngressoResponseDTO> response = ingressoService.listarDoUsuario(usuarioId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }
}