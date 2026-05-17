package br.com.softhouse.dende.services;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.dto.request.UsuarioRequestDTO;
import br.com.softhouse.dende.dto.response.UsuarioResponseDTO;
import br.com.softhouse.dende.exceptions.*;
import br.com.softhouse.dende.mapper.UsuarioMapper;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.UsuarioRepository;

@Component
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        validarCamposObrigatorios(dto);

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException(dto.getEmail(), "usuário");
        }

        Usuario usuario = UsuarioMapper.toModel(dto);
        usuario.setTipoUsuario("COMUM");
        usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioResponseDTO alterar(long usuarioId, UsuarioRequestDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));

        if (!usuarioExistente.getEmail().equals(dto.getEmail())) {
            throw new RegraDeNegocioException("Não é permitido alterar o e-mail do usuário.");
        }

        UsuarioMapper.updateModel(dto, usuarioExistente);
        usuarioRepository.save(usuarioExistente);
        return UsuarioMapper.toResponse(usuarioExistente);
    }

    public UsuarioResponseDTO buscarPorId(long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));
        return UsuarioMapper.toResponse(usuario);
    }

    public void alterarStatus(long usuarioId, String status) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));

        if ("ativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(true);
        } else if ("desativar".equalsIgnoreCase(status)) {
            usuario.setAtivo(false);
        } else {
            throw new RegraDeNegocioException("Status inválido. Use 'ativar' ou 'desativar'.");
        }

        usuarioRepository.save(usuario);
    }

    private void validarCamposObrigatorios(UsuarioRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty())
            throw new RegraDeNegocioException("Nome é obrigatório.");
        if (dto.getDataNascimento() == null)
            throw new RegraDeNegocioException("Data de nascimento é obrigatória.");
        if (dto.getSexo() == null || dto.getSexo().trim().isEmpty())
            throw new RegraDeNegocioException("Sexo é obrigatório.");
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty())
            throw new RegraDeNegocioException("Email é obrigatório.");
        if (dto.getSenha() == null || dto.getSenha().trim().isEmpty())
            throw new RegraDeNegocioException("Senha é obrigatória.");
    }
}
