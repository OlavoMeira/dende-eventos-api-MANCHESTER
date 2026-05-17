package br.com.softhouse.dende.services;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.exceptions.EmailJaCadastradoException;
import br.com.softhouse.dende.exceptions.RecursoNaoEncontradoException;
import br.com.softhouse.dende.exceptions.RegraDeNegocioException;
import br.com.softhouse.dende.exceptions.OrganizadorComEventosAtivosException;
import br.com.softhouse.dende.mapper.OrganizadorMapper;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.OrganizadorRepository;

@Component
public class OrganizadorService {

    private final OrganizadorRepository organizadorRepository;

    public OrganizadorService(OrganizadorRepository organizadorRepository) {
        this.organizadorRepository = organizadorRepository;
    }

    public OrganizadorResponseDTO cadastrar(OrganizadorRequestDTO dto) {
        validarCamposObrigatorios(dto);

        if (organizadorRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException(dto.getEmail(), "organizador");
        }

        Organizador organizador = OrganizadorMapper.toModel(dto);
        organizador.setTipoUsuario("ORGANIZADOR");
        organizadorRepository.save(organizador);
        return OrganizadorMapper.toResponse(organizador);
    }

    public OrganizadorResponseDTO alterar(long organizadorId, OrganizadorRequestDTO dto) {
        Organizador organizadorExistente = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        if (!organizadorExistente.getEmail().equals(dto.getEmail())) {
            throw new RegraDeNegocioException("Não é permitido alterar o e-mail do organizador.");
        }

        OrganizadorMapper.updateModel(dto, organizadorExistente);
        organizadorRepository.save(organizadorExistente);
        return OrganizadorMapper.toResponse(organizadorExistente);
    }

    public OrganizadorResponseDTO buscarPorId(long organizadorId) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));
        return OrganizadorMapper.toResponse(organizador);
    }

    public void alterarStatus(long organizadorId, String status) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        if ("ativar".equalsIgnoreCase(status)) {
            organizador.setAtivo(true);
        } else if ("desativar".equalsIgnoreCase(status)) {
            if (organizadorRepository.possuiEventosAtivos(organizadorId)) {
                throw new OrganizadorComEventosAtivosException(organizadorId);
            }
            organizador.setAtivo(false);
        } else {
            throw new RegraDeNegocioException("Status inválido. Use 'ativar' ou 'desativar'.");
        }

        organizadorRepository.save(organizador);
    }

    private void validarCamposObrigatorios(OrganizadorRequestDTO dto) {
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
