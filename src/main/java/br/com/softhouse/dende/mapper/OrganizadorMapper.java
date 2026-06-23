package br.com.softhouse.dende.mapper;

import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.model.Empresa;
import br.com.softhouse.dende.model.Organizador;

public class OrganizadorMapper {

    private OrganizadorMapper() {

    }

    
    public static Organizador toModel(OrganizadorRequestDTO dto) {
        if (dto == null) return null;

        Organizador organizador = new Organizador();
        organizador.setNome(dto.getNome());
        organizador.setDataNascimento(dto.getDataNascimento());
        organizador.setSexo(dto.getSexo());
        organizador.setEmail(dto.getEmail());
        organizador.setSenha(dto.getSenha());

        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            Empresa empresa = new Empresa(
                    dto.getCnpj(),
                    dto.getRazaoSocial(),
                    dto.getNomeFantasia(),
                    null
            );
            organizador.setEmpresa(empresa);
        }

        return organizador;
    }

    
    public static void updateModel(OrganizadorRequestDTO dto, Organizador organizadorExistente) {
        if (dto == null || organizadorExistente == null) return;

        organizadorExistente.setNome(dto.getNome());
        organizadorExistente.setDataNascimento(dto.getDataNascimento());
        organizadorExistente.setSexo(dto.getSexo());

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            organizadorExistente.setSenha(dto.getSenha());
        }

        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            Empresa empresa = organizadorExistente.getEmpresa() != null
                    ? organizadorExistente.getEmpresa()
                    : new Empresa();
            empresa.setCnpj(dto.getCnpj());
            empresa.setRazaoSocial(dto.getRazaoSocial());
            empresa.setNomeFantasia(dto.getNomeFantasia());
            organizadorExistente.setEmpresa(empresa);
        }
    }

    
    public static OrganizadorResponseDTO toResponse(Organizador organizador) {
        if (organizador == null) return null;

        OrganizadorResponseDTO dto = new OrganizadorResponseDTO();
        dto.setId(organizador.getId());
        dto.setNome(organizador.getNome());
        dto.setDataNascimento(organizador.getDataNascimento() != null
                ? organizador.getDataNascimento().toString() : null);
        dto.setIdadeCompleta(organizador.getIdadeCompleta());
        dto.setSexo(organizador.getSexo());
        dto.setEmail(organizador.getEmail());
        dto.setAtivo(organizador.isAtivo());

        if (organizador.isEmpresa()) {
            OrganizadorResponseDTO.EmpresaResponseDTO empresaDTO =
                    new OrganizadorResponseDTO.EmpresaResponseDTO();
            empresaDTO.setCnpj(organizador.getCnpj());
            empresaDTO.setRazaoSocial(organizador.getRazaoSocial());
            empresaDTO.setNomeFantasia(organizador.getNomeFantasia());
            dto.setEmpresa(empresaDTO);
        }

        return dto;
    }
}
