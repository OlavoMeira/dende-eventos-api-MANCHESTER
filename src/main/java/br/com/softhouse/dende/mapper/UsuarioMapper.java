package br.com.softhouse.dende.mapper;

import br.com.softhouse.dende.dto.request.UsuarioRequestDTO;
import br.com.softhouse.dende.dto.response.UsuarioResponseDTO;
import br.com.softhouse.dende.model.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {

    }

    
    public static Usuario toModel(UsuarioRequestDTO dto) {
        if (dto == null) return null;

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSexo(dto.getSexo());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    
    public static void updateModel(UsuarioRequestDTO dto, Usuario usuarioExistente) {
        if (dto == null || usuarioExistente == null) return;

        usuarioExistente.setNome(dto.getNome());
        usuarioExistente.setDataNascimento(dto.getDataNascimento());
        usuarioExistente.setSexo(dto.getSexo());

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            usuarioExistente.setSenha(dto.getSenha());
        }
    }

    
    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setDataNascimento(usuario.getDataNascimento() != null
                ? usuario.getDataNascimento().toString() : null);
        dto.setIdadeCompleta(usuario.getIdadeCompleta());
        dto.setSexo(usuario.getSexo());
        dto.setEmail(usuario.getEmail());
        dto.setAtivo(usuario.isAtivo());
        return dto;
    }
}
