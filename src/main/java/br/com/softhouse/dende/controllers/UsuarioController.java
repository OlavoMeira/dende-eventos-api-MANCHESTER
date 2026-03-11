package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.UsuarioRequestDTO;
import br.com.softhouse.dende.dto.response.UsuarioResponseDTO;
import br.com.softhouse.dende.mapper.UsuarioMapper;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.Optional;

@Controller
@RequestMapping(path = "/usuarios")
public class UsuarioController {

    private final Repositorio repositorio;

    public UsuarioController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping
    public ResponseEntity<String> cadastroUsuario(@RequestBody UsuarioRequestDTO dto) {

        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome é obrigatório");
        }
        if (dto.getDataNascimento() == null) {
            return ResponseUtils.badRequest("Data de nascimento é obrigatória");
        }
        if (dto.getSexo() == null || dto.getSexo().trim().isEmpty()) {
            return ResponseUtils.badRequest("Sexo é obrigatório");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseUtils.badRequest("Email é obrigatório");
        }
        if (dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
            return ResponseUtils.badRequest("Senha é obrigatória");
        }

        if (repositorio.existeUsuarioComEmail(dto.getEmail())) {
            return ResponseUtils.badRequest("Já existe um usuário cadastrado com este e-mail: " + dto.getEmail());
        }
        if (repositorio.existeOrganizadorComEmail(dto.getEmail())) {
            return ResponseUtils.badRequest("Este e-mail já está cadastrado como organizador: " + dto.getEmail());
        }

        Usuario usuario = UsuarioMapper.toModel(dto);
        repositorio.salvarUsuario(usuario);

        return ResponseUtils.ok("Usuario " + usuario.getEmail() + " registrado com sucesso!");
    }

    @PutMapping(path = "/{usuarioId}")
    public ResponseEntity<String> alterarUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @RequestBody UsuarioRequestDTO dto) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuario usuarioExistente = usuarioOpt.get();

        if (!usuarioExistente.getEmail().equals(dto.getEmail())) {
            return ResponseUtils.badRequest("Não é permitido alterar o e-mail do usuário");
        }

        UsuarioMapper.updateModel(dto, usuarioExistente);
        repositorio.salvarUsuario(usuarioExistente);

        return ResponseUtils.ok("Usuario " + usuarioExistente.getEmail() + " alterado com sucesso!");
    }

    @GetMapping(path = "/{usuarioId}")
    public ResponseEntity<?> visualizarPerfil(@PathVariable(parameter = "usuarioId") long usuarioId) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);
        }

        UsuarioResponseDTO responseDTO = UsuarioMapper.toResponse(usuarioOpt.get());
        return ResponseUtils.ok(responseDTO);
    }

    @PatchMapping(path = "/{usuarioId}/{status}")
    public ResponseEntity<String> alterarStatus(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @PathVariable(parameter = "status") String status) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuario usuario = usuarioOpt.get();

        if ("ativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(true);
            repositorio.salvarUsuario(usuario);
            return ResponseUtils.ok("Usuário reativado com sucesso!");
        } else if ("desativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(false);
            repositorio.salvarUsuario(usuario);
            return ResponseUtils.ok("Usuário desativado com sucesso!");
        } else {
            return ResponseUtils.badRequest("Status inválido. Use 'ativar' ou 'desativar'");
        }
    }
}
