package br.com.softhouse.dende.mapper;

import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.model.Empresa;
import br.com.softhouse.dende.model.Organizador;

public class OrganizadorMapper {

    public static Organizador toModel(OrganizadorRequestDTO dto) {
        Organizador o = new Organizador();
        o.setNome(dto.getNome());
        o.setDataNascimento(dto.getDataNascimento());
        o.setSexo(dto.getSexo());
        o.setEmail(dto.getEmail());
        o.setSenha(dto.getSenha());
        o.setAtivo(true);
        o.setTipoUsuario("ORGANIZADOR");

        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            o.setEmpresa(new Empresa(dto.getCnpj(), dto.getRazaoSocial(), dto.getNomeFantasia(), null));
        }
        return o;
    }

    public static void updateModel(OrganizadorRequestDTO dto, Organizador o) {
        o.setNome(dto.getNome());
        o.setDataNascimento(dto.getDataNascimento());
        o.setSexo(dto.getSexo());
        o.setSenha(dto.getSenha());

        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            if (o.getEmpresa() == null) {
                o.setEmpresa(new Empresa(dto.getCnpj(), dto.getRazaoSocial(), dto.getNomeFantasia(), o.getId()));
            } else {
                o.getEmpresa().setCnpj(dto.getCnpj());
                o.getEmpresa().setRazaoSocial(dto.getRazaoSocial());
                o.getEmpresa().setNomeFantasia(dto.getNomeFantasia());
            }
        } else {
            o.setEmpresa(null);
        }
    }

    public static OrganizadorResponseDTO toResponse(Organizador o) {
        OrganizadorResponseDTO dto = new OrganizadorResponseDTO();
        dto.setId(o.getId());
        dto.setNome(o.getNome());
        dto.setDataNascimento(o.getDataNascimento() != null ? o.getDataNascimento().toString() : null);
        dto.setSexo(o.getSexo());
        dto.setEmail(o.getEmail());
        dto.setAtivo(o.isAtivo());

        if (o.isEmpresa()) {
            OrganizadorResponseDTO.EmpresaResponseDTO empresaDto = new OrganizadorResponseDTO.EmpresaResponseDTO();
            empresaDto.setCnpj(o.getEmpresa().getCnpj());
            empresaDto.setRazaoSocial(o.getEmpresa().getRazaoSocial());
            empresaDto.setNomeFantasia(o.getEmpresa().getNomeFantasia());
            dto.setEmpresa(empresaDto);
        }

        return dto;
    }
}
