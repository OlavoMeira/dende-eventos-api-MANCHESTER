package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.UsuarioRequestDTO;
import br.com.softhouse.dende.dto.response.UsuarioResponseDTO;
import br.com.softhouse.dende.services.UsuarioService;

@Controller
@RequestMapping(path = "/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController() {
        this.usuarioService = new UsuarioService();
    }

    @PostMapping
    public ResponseEntity<Object> cadastrarUsuario(@RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioResponseDTO response = usuarioService.cadastrar(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PutMapping(path = "/{usuarioId}")
    public ResponseEntity<Object> alterarUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioResponseDTO response = usuarioService.alterar(usuarioId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @GetMapping(path = "/{usuarioId}")
    public ResponseEntity<Object> visualizarPerfil(
            @PathVariable(parameter = "usuarioId") long usuarioId) {
        try {
            UsuarioResponseDTO response = usuarioService.buscarPorId(usuarioId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @PatchMapping(path = "/{usuarioId}/{status}")
    public ResponseEntity<Object> alterarStatus(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @PathVariable(parameter = "status") String status) {
        try {
            usuarioService.alterarStatus(usuarioId, status);
            return ResponseEntity.ok("Status do usuário atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }
}