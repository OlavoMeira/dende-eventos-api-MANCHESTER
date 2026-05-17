package br.com.softhouse.dende.services;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.exceptions.RecursoNaoEncontradoException;
import br.com.softhouse.dende.exceptions.RegraDeNegocioException;
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
            throw new RegraDeNegocioException("Organizador inativo não pode cadastrar eventos.");
        }

        validarCamposObrigatorios(dto);
        validarDatas(dto.getDataInicio(), dto.getDataFim());

        Evento evento = EventoMapper.toModel(dto);
        evento.setOrganizador(organizador);

        if (dto.getEventoPrincipalId() != null) {
            Evento principal = eventoRepository.findById(dto.getEventoPrincipalId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Evento Principal", dto.getEventoPrincipalId()));
            evento.setEventoPrincipal(principal);
        }

        eventoRepository.save(evento);
        return EventoMapper.toResponse(evento);
    }

    public EventoResponseDTO alterar(long organizadorId, long eventoId, EventoRequestDTO dto) {
        Evento eventoExistente = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", eventoId));

        if (eventoExistente.getOrganizador().getId() != organizadorId) {
            throw new RegraDeNegocioException("O evento não pertence ao organizador informado.");
        }

        if (!eventoExistente.isAtivo()) {
            throw new RegraDeNegocioException("Não é permitido alterar um evento inativo.");
        }

        if (dto.getDataInicio() != null || dto.getDataFim() != null) {
            LocalDateTime inicio = dto.getDataInicio() != null ? dto.getDataInicio() : eventoExistente.getDataInicio();
            LocalDateTime fim = dto.getDataFim() != null ? dto.getDataFim() : eventoExistente.getDataFim();
            validarDatas(inicio, fim);
        }

        EventoMapper.updateModel(dto, eventoExistente);
        eventoRepository.save(eventoExistente);
        return EventoMapper.toResponse(eventoExistente);
    }

    public void alterarStatus(long organizadorId, long eventoId, String status) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", eventoId));

        if (evento.getOrganizador().getId() != organizadorId) {
            throw new RegraDeNegocioException("O evento não pertence ao organizador informado.");
        }

        if ("ativar".equalsIgnoreCase(status)) {
            evento.setAtivo(true);
        } else if ("desativar".equalsIgnoreCase(status)) {
            evento.setAtivo(false);
            if (evento.isEstornaIngresso()) {
                eventoRepository.cancelarIngressosDoEvento(eventoId, evento.getTaxaCancelamento());
            }
        } else {
            throw new RegraDeNegocioException("Status inválido. Use 'ativar' ou 'desativar'.");
        }

        eventoRepository.save(evento);
    }

    public List<EventoResponseDTO> listarDoOrganizador(long organizadorId) {
        return eventoRepository.findByOrganizadorId(organizadorId).stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventoResponseDTO> listarFeed() {
        return eventoRepository.findAtivosComVagas().stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarCamposObrigatorios(EventoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty())
            throw new RegraDeNegocioException("Nome é obrigatório.");
        if (dto.getTipoEvento() == null)
            throw new RegraDeNegocioException("Tipo do evento é obrigatório.");
        if (dto.getModalidade() == null)
            throw new RegraDeNegocioException("Modalidade é obrigatória.");
        if (dto.getLocal() == null || dto.getLocal().trim().isEmpty())
            throw new RegraDeNegocioException("Local é obrigatório.");
        if (dto.getCapacidadeMaxima() == null || dto.getCapacidadeMaxima() <= 0)
            throw new RegraDeNegocioException("Capacidade máxima deve ser maior que zero.");
    }

    private void validarDatas(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null)
            throw new RegraDeNegocioException("Datas de início e fim são obrigatórias.");
        
        if (inicio.isBefore(LocalDateTime.now()))
            throw new RegraDeNegocioException("A data de início não pode ser no passado.");
        
        if (fim.isBefore(inicio))
            throw new RegraDeNegocioException("A data de fim deve ser após a data de início.");
        
        Duration duracao = Duration.between(inicio, fim);
        if (duracao.toMinutes() < 30) {
            throw new RegraDeNegocioException("O evento deve ter duração mínima de 30 minutos.");
        }
    }
}
