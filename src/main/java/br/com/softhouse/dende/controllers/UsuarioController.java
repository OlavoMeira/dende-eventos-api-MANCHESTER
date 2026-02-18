package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(path = "/usuarios")
public class UsuarioController {

    private final Repositorio repositorio;

    public UsuarioController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping
    public ResponseEntity<String> cadastroUsuario(@RequestBody Usuario usuario) {
        // Validar campos obrigatórios
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome é obrigatório");
        }
        if (usuario.getDataNascimento() == null) {
            return ResponseUtils.badRequest("Data de nascimento é obrigatória");
        }
        if (usuario.getSexo() == null || usuario.getSexo().trim().isEmpty()) {
            return ResponseUtils.badRequest("Sexo é obrigatório");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return ResponseUtils.badRequest("Email é obrigatório");
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            return ResponseUtils.badRequest("Senha é obrigatória");
        }

        // Verificar email duplicado
        if (repositorio.existeUsuarioComEmail(usuario.getEmail())) {
            return ResponseUtils.badRequest("Já existe um usuário cadastrado com este e-mail: " + usuario.getEmail());
        }

        if (repositorio.existeOrganizadorComEmail(usuario.getEmail())) {
            return ResponseUtils.badRequest("Este e-mail já está cadastrado como organizador: " + usuario.getEmail());
        }

        repositorio.salvarUsuario(usuario);

        return ResponseUtils.ok("Usuario " + usuario.getEmail() + " registrado com sucesso!");
    }

    @PutMapping(path = "/{usuarioId}")
    public ResponseEntity<String> alterarUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @RequestBody Usuario usuario) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuario usuarioExistente = usuarioOpt.get();

        // Não permitir alteração de e-mail
        if (!usuarioExistente.getEmail().equals(usuario.getEmail())) {
            return ResponseUtils.badRequest("Não é permitido alterar o e-mail do usuário");
        }

        // Manter o ID
        usuario.setId(usuarioId);

        // Manter a senha se não foi fornecida
        if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
            usuario.setSenha(usuarioExistente.getSenha());
        }

        // Manter o status ativo
        usuario.setAtivo(usuarioExistente.isAtivo());

        repositorio.salvarUsuario(usuario);

        return ResponseUtils.ok("Usuario " + usuario.getEmail() + " alterado com sucesso!");
    }

    @GetMapping(path = "/{usuarioId}")
    public ResponseEntity<Object> visualizarPerfil(@PathVariable(parameter = "usuarioId") long usuarioId) {
        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            // Retorna um Map com erro em vez de String
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Usuário não encontrado com ID: " + usuarioId);
            return ResponseUtils.ok(erro); // Agora retorna Object
        }

        Usuario usuario = usuarioOpt.get();

        // Criar um mapa com os dados para não expor a senha
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("nome", usuario.getNome());
        perfil.put("dataNascimento", usuario.getDataNascimento().toString());
        perfil.put("idadeCompleta", usuario.getIdadeCompleta());
        perfil.put("sexo", usuario.getSexo());
        perfil.put("email", usuario.getEmail());
        perfil.put("ativo", usuario.isAtivo());

        return ResponseUtils.ok(perfil);
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