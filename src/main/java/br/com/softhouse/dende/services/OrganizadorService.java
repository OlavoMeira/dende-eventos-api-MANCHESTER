package br.com.softhouse.dende.services;

import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.mapper.OrganizadorMapper;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.Repositorio;

public class OrganizadorService {

    private final Repositorio repositorio;

    public OrganizadorService() {
        this.repositorio = Repositorio.getInstance();
    }

    public OrganizadorResponseDTO cadastrar(OrganizadorRequestDTO dto) {
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

        if (repositorio.existeOrganizadorComEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Já existe um organizador cadastrado com este e-mail: " + dto.getEmail());
        }
        if (repositorio.existeUsuarioComEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado como usuário comum: " + dto.getEmail());
        }

        Organizador organizador = OrganizadorMapper.toModel(dto);
        repositorio.salvarOrganizador(organizador);

        return OrganizadorMapper.toResponse(organizador);
    }

    public OrganizadorResponseDTO alterar(long organizadorId, OrganizadorRequestDTO dto) {
        Organizador organizadorExistente = repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        if (!organizadorExistente.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("Não é permitido alterar o e-mail do organizador");
        }

        OrganizadorMapper.updateModel(dto, organizadorExistente);
        repositorio.salvarOrganizador(organizadorExistente);

        return OrganizadorMapper.toResponse(organizadorExistente);
    }

    public OrganizadorResponseDTO buscarPorId(long organizadorId) {
        Organizador organizador = repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        return OrganizadorMapper.toResponse(organizador);
    }

    public void alterarStatus(long organizadorId, String status) {
        Organizador organizador = repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        if ("ativar".equalsIgnoreCase(status)) {
            organizador.setAtivo(true);
        } else if ("desativar".equalsIgnoreCase(status)) {
            if (repositorio.organizadorTemEventosAtivos(organizadorId)) {
                throw new IllegalArgumentException(
                        "Não é possível desativar o organizador pois ele possui eventos ativos ou em execução");
            }
            organizador.setAtivo(false);
        } else {
            throw new IllegalArgumentException("Status inválido. Use 'ativar' ou 'desativar'");
        }

        repositorio.salvarOrganizador(organizador);
    }
}