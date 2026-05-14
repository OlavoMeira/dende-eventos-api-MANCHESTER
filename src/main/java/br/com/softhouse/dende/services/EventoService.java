package br.com.softhouse.dende.services;

import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.mapper.EventoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.Repositorio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EventoService {

    private final Repositorio repositorio;

    public EventoService() {
        this.repositorio = Repositorio.getInstance();
    }

    public EventoResponseDTO cadastrar(long organizadorId, EventoRequestDTO dto) {
        Organizador organizador = repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        if (!organizador.isAtivo()) {
            throw new IllegalArgumentException("Organizador está desativado. Não é possível cadastrar eventos.");
        }

        validarCamposObrigatorios(dto);
        validarDatas(dto.getDataInicio(), dto.getDataFim());

        Evento eventoPrincipal = null;
        if (dto.getEventoPrincipalId() != null) {
            eventoPrincipal = repositorio.buscarEventoPorId(dto.getEventoPrincipalId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Evento principal não encontrado com ID: " + dto.getEventoPrincipalId()));
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

        repositorio.salvarEvento(evento);

        return EventoMapper.toResponse(evento);
    }

    public EventoResponseDTO alterar(long organizadorId, long eventoId, EventoRequestDTO dto) {
        repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        Evento eventoExistente = repositorio.buscarEventoPorId(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com ID: " + eventoId));

        if (!eventoExistente.getOrganizador().getId().equals(organizadorId)) {
            throw new IllegalArgumentException("Este evento não pertence ao organizador informado");
        }

        if (!eventoExistente.isAtivo()) {
            throw new IllegalArgumentException("Não é possível alterar um evento inativo");
        }

        if (dto.getDataInicio() != null && dto.getDataFim() != null) {
            validarDatas(dto.getDataInicio(), dto.getDataFim());
        }

        EventoMapper.updateModel(dto, eventoExistente);

        repositorio.salvarEvento(eventoExistente);

        return EventoMapper.toResponse(eventoExistente);
    }

    public void alterarStatus(long organizadorId, long eventoId, String status) {
        repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        Evento evento = repositorio.buscarEventoPorId(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com ID: " + eventoId));

        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            throw new IllegalArgumentException("Este evento não pertence ao organizador informado");
        }

        if ("ativar".equalsIgnoreCase(status)) {
            evento.setAtivo(true);
            repositorio.salvarEvento(evento);
        } else if ("desativar".equalsIgnoreCase(status)) {
            evento.setAtivo(false);
            repositorio.salvarEvento(evento);
            repositorio.cancelarIngressosDoEvento(eventoId);
        } else {
            throw new IllegalArgumentException("Status inválido. Use 'ativar' ou 'desativar'");
        }
    }

    public List<EventoResponseDTO> listarDoOrganizador(long organizadorId) {
        repositorio.buscarOrganizadorPorId(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + organizadorId));

        return repositorio.listarEventosDoOrganizador(organizadorId)
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventoResponseDTO> listarFeed() {
        return repositorio.listarEventosAtivos()
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarCamposObrigatorios(EventoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do evento é obrigatório");
        }
        if (dto.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início é obrigatória");
        }
        if (dto.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim é obrigatória");
        }
        if (dto.getTipoEvento() == null) {
            throw new IllegalArgumentException("Tipo de evento é obrigatório");
        }
        if (dto.getModalidade() == null) {
            throw new IllegalArgumentException("Modalidade é obrigatória");
        }
        if (dto.getCapacidadeMaxima() == null || dto.getCapacidadeMaxima() <= 0) {
            throw new IllegalArgumentException("Capacidade máxima deve ser maior que zero");
        }
        if (dto.getPrecoUnitarioIngresso() == null) {
            throw new IllegalArgumentException("Preço do ingresso é obrigatório");
        }
    }

    private void validarDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        LocalDateTime agora = LocalDateTime.now();

        if (dataInicio.isBefore(agora)) {
            throw new IllegalArgumentException("A data de início do evento não pode ser anterior à data atual");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim do evento não pode ser anterior à data de início");
        }

        Duration duracao = Duration.between(dataInicio, dataFim);
        if (duracao.toMinutes() < 30) {
            throw new IllegalArgumentException("O evento deve ter no mínimo 30 minutos de duração");
        }
    }
}