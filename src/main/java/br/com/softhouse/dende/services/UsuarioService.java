package br.com.softhouse.dende.services;

import br.com.softhouse.dende.dto.request.UsuarioRequestDTO;
import br.com.softhouse.dende.dto.response.UsuarioResponseDTO;
import br.com.softhouse.dende.mapper.UsuarioMapper;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.Repositorio;

public class UsuarioService {

    private final Repositorio repositorio;

    public UsuarioService() {
        this.repositorio = Repositorio.getInstance();
    }

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (dto.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        if (dto.getSexo() == null || dto.getSexo().trim().isEmpty()) {
            throw new IllegalArgumentException("Sexo é obrigatório");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        if (repositorio.existeUsuarioComEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail: " + dto.getEmail());
        }
        if (repositorio.existeOrganizadorComEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado como organizador: " + dto.getEmail());
        }

        Usuario usuario = UsuarioMapper.toModel(dto);
        repositorio.salvarUsuario(usuario);

        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioResponseDTO alterar(long usuarioId, UsuarioRequestDTO dto) {
        Usuario usuarioExistente = repositorio.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        if (!usuarioExistente.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("Não é permitido alterar o e-mail do usuário");
        }

        UsuarioMapper.updateModel(dto, usuarioExistente);
        repositorio.salvarUsuario(usuarioExistente);

        return UsuarioMapper.toResponse(usuarioExistente);
    }

    public UsuarioResponseDTO buscarPorId(long usuarioId) {
        Usuario usuario = repositorio.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        return UsuarioMapper.toResponse(usuario);
    }

    public void alterarStatus(long usuarioId, String status) {
        Usuario usuario = repositorio.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        if ("ativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(true);
        } else if ("desativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(false);
        } else {
            throw new IllegalArgumentException("Status inválido. Use 'ativar' ou 'desativar'");
        }

        repositorio.salvarUsuario(usuario);
    }
}