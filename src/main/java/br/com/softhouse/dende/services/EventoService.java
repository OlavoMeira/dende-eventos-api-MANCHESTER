package br.com.softhouse.dende.services;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.exceptions.*;
import br.com.softhouse.dende.mapper.EventoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.EventoRepository;
import br.com.softhouse.dende.repositories.OrganizadorRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventoService {

    private final EventoRepository eventoRepository;
    private final OrganizadorRepository organizadorRepository;

    public EventoService(EventoRepository eventoRepository,
                         OrganizadorRepository organizadorRepository) {
        this.eventoRepository = eventoRepository;
        this.organizadorRepository = organizadorRepository;
    }

    public EventoResponseDTO cadastrar(long organizadorId, EventoRequestDTO dto) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        if (!organizador.isAtivo()) {
            throw new RegraDeNegocioException("Organizador está desativado. Não é possível cadastrar eventos.");
        }

        validarCamposObrigatorios(dto);
        validarDatas(dto.getDataInicio(), dto.getDataFim());

        Evento eventoPrincipal = null;
        if (dto.getEventoPrincipalId() != null) {
            eventoPrincipal = eventoRepository.findById(dto.getEventoPrincipalId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Evento principal", dto.getEventoPrincipalId()));
        }

        Evento evento = Evento.builder()
                .nome(dto.getNome())
                .paginaWeb(dto.getPaginaWeb())
                .descricao(dto.getDescricao())
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .tipoEvento(dto.getTipoEvento())
                .modalidade(dto.getModalidade())
                .local(dto.getLocal())
                .capacidadeMaxima(dto.getCapacidadeMaxima())
                .precoUnitarioIngresso(dto.getPrecoUnitarioIngresso())
                .taxaCancelamento(dto.getTaxaCancelamento())
                .ativo(true)
                .organizador(organizador)
                .eventoPrincipal(eventoPrincipal)
                .build();

        eventoRepository.save(evento);
        return EventoMapper.toResponse(evento);
    }

    public EventoResponseDTO alterar(long organizadorId, long eventoId, EventoRequestDTO dto) {
        organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        Evento eventoExistente = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", eventoId));

        if (!eventoExistente.getOrganizador().getId().equals(organizadorId)) {
            throw new RegraDeNegocioException("Este evento não pertence ao organizador informado.");
        }
        if (!eventoExistente.isAtivo()) {
            throw new RegraDeNegocioException("Não é possível alterar um evento inativo.");
        }
        if (dto.getDataInicio() != null && dto.getDataFim() != null) {
            validarDatas(dto.getDataInicio(), dto.getDataFim());
        }

        EventoMapper.updateModel(dto, eventoExistente);
        eventoRepository.save(eventoExistente);
        return EventoMapper.toResponse(eventoExistente);
    }

    public void alterarStatus(long organizadorId, long eventoId, String status) {
        organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", eventoId));

        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            throw new RegraDeNegocioException("Este evento não pertence ao organizador informado.");
        }

        if ("ativar".equalsIgnoreCase(status)) {
            evento.setAtivo(true);
            eventoRepository.save(evento);
        } else if ("desativar".equalsIgnoreCase(status)) {
            evento.setAtivo(false);
            eventoRepository.save(evento);
            eventoRepository.cancelarIngressosDoEvento(eventoId, evento.getTaxaCancelamento());
        } else {
            throw new RegraDeNegocioException("Status inválido. Use 'ativar' ou 'desativar'.");
        }
    }

    public List<EventoResponseDTO> listarDoOrganizador(long organizadorId) {
        organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Organizador", organizadorId));

        return eventoRepository.findByOrganizadorId(organizadorId)
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventoResponseDTO> listarFeed() {
        return eventoRepository.findAtivosComVagas()
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarCamposObrigatorios(EventoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty())
            throw new RegraDeNegocioException("Nome do evento é obrigatório.");
        if (dto.getDataInicio() == null)
            throw new RegraDeNegocioException("Data de início é obrigatória.");
        if (dto.getDataFim() == null)
            throw new RegraDeNegocioException("Data de fim é obrigatória.");
        if (dto.getTipoEvento() == null)
            throw new RegraDeNegocioException("Tipo de evento é obrigatório.");
        if (dto.getModalidade() == null)
            throw new RegraDeNegocioException("Modalidade é obrigatória.");
        if (dto.getCapacidadeMaxima() == null || dto.getCapacidadeMaxima() <= 0)
            throw new RegraDeNegocioException("Capacidade máxima deve ser maior que zero.");
        if (dto.getPrecoUnitarioIngresso() == null)
            throw new RegraDeNegocioException("Preço do ingresso é obrigatório.");
    }

    private void validarDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        LocalDateTime agora = LocalDateTime.now();
        if (dataInicio.isBefore(agora))
            throw new RegraDeNegocioException("A data de início do evento não pode ser anterior à data atual.");
        if (dataFim.isBefore(dataInicio))
            throw new RegraDeNegocioException("A data de fim não pode ser anterior à data de início.");
        if (Duration.between(dataInicio, dataFim).toMinutes() < 30)
            throw new RegraDeNegocioException("O evento deve ter no mínimo 30 minutos de duração.");
    }
}