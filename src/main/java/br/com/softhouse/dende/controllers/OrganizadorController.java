package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.services.OrganizadorService;

@Controller
@RequestMapping(path = "/organizadores")
public class OrganizadorController {

    private final OrganizadorService organizadorService;

    public OrganizadorController() {
        this.organizadorService = new OrganizadorService();
    }

    @PostMapping
    public ResponseEntity<Object> cadastrarOrganizador(@RequestBody OrganizadorRequestDTO dto) {
        try {
            OrganizadorResponseDTO response = organizadorService.cadastrar(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PutMapping(path = "/{organizadorId}")
    public ResponseEntity<Object> alterarOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @RequestBody OrganizadorRequestDTO dto) {
        try {
            OrganizadorResponseDTO response = organizadorService.alterar(organizadorId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @GetMapping(path = "/{organizadorId}")
    public ResponseEntity<Object> visualizarPerfil(
            @PathVariable(parameter = "organizadorId") long organizadorId) {
        try {
            OrganizadorResponseDTO response = organizadorService.buscarPorId(organizadorId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PatchMapping(path = "/{organizadorId}/{status}")
    public ResponseEntity<Object> alterarStatus(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "status") String status) {
        try {
            organizadorService.alterarStatus(organizadorId, status);
            return ResponseEntity.ok("Status do organizador atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }
}