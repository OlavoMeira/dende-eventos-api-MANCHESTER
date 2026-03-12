package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.mapper.EventoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.model.builder.EventoBuilder;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/organizadores")
public class EventoController {

    private final Repositorio repositorio;

    public EventoController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping(path = "/{organizadorId}/eventos")
    public ResponseEntity<String> cadastrarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @RequestBody EventoRequestDTO eventoRequest) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizador = organizadorOpt.get();

        if (!organizador.isAtivo()) {
            return ResponseUtils.badRequest("Organizador está desativado. Não é possível cadastrar eventos.");
        }

        String validacao = validarEventoRequest(eventoRequest);
        if (validacao != null) {
            return ResponseUtils.badRequest(validacao);
        }

        EventoBuilder builder = EventoBuilder.builder()
                .nome(eventoRequest.getNome())
                .paginaWeb(eventoRequest.getPaginaWeb())
                .descricao(eventoRequest.getDescricao())
                .dataInicio(eventoRequest.getDataInicio())
                .dataFim(eventoRequest.getDataFim())
                .tipoEvento(eventoRequest.getTipoEvento())
                .modalidade(eventoRequest.getModalidade())
                .local(eventoRequest.getLocal())
                .capacidadeMaxima(eventoRequest.getCapacidadeMaxima())
                .precoUnitarioIngresso(eventoRequest.getPrecoUnitarioIngresso())
                .taxaCancelamento(eventoRequest.getTaxaCancelamento())
                .ativo(true)
                .organizador(organizador);

        if (eventoRequest.getEventoPrincipalId() != null) {
            Optional<Evento> eventoPrincipalOpt = repositorio.buscarEventoPorId(eventoRequest.getEventoPrincipalId());
            eventoPrincipalOpt.ifPresent(builder::eventoPrincipal);
        }

        Evento evento = builder.build();
        repositorio.salvarEvento(evento);

        return ResponseUtils.ok("Evento " + evento.getNome() + " cadastrado com sucesso! ID: " + evento.getId());
    }

    @PutMapping(path = "/{organizadorId}/eventos/{eventoId}")
    public ResponseEntity<String> alterarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody EventoRequestDTO eventoRequest) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Optional<Evento> eventoOpt = repositorio.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent()) {
            return ResponseUtils.badRequest("Evento não encontrado com ID: " + eventoId);
        }

        Evento eventoExistente = eventoOpt.get();

        if (!eventoExistente.getOrganizador().getId().equals(organizadorId)) {
            return ResponseUtils.badRequest("Este evento não pertence ao organizador informado");
        }

        if (!eventoExistente.isAtivo()) {
            return ResponseUtils.badRequest("Não é possível alterar um evento inativo");
        }

        if (eventoRequest.getDataInicio() != null && eventoRequest.getDataFim() != null) {
            String validacao = validarDatasEvento(eventoRequest.getDataInicio(), eventoRequest.getDataFim());
            if (validacao != null) {
                return ResponseUtils.badRequest(validacao);
            }
        }

        EventoMapper.updateModel(eventoRequest, eventoExistente);

        if (eventoRequest.getEventoPrincipalId() != null) {
            Optional<Evento> eventoPrincipalOpt = repositorio.buscarEventoPorId(eventoRequest.getEventoPrincipalId());
            eventoPrincipalOpt.ifPresent(eventoExistente::setEventoPrincipal);
        }

        repositorio.salvarEvento(eventoExistente);

        return ResponseUtils.ok("Evento " + eventoExistente.getNome() + " alterado com sucesso!");
    }

    @PatchMapping(path = "/{organizadorId}/eventos/{eventoId}/{status}")
    public ResponseEntity<String> alterarStatusEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @PathVariable(parameter = "status") String status) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Optional<Evento> eventoOpt = repositorio.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent()) {
            return ResponseUtils.badRequest("Evento não encontrado com ID: " + eventoId);
        }

        Evento evento = eventoOpt.get();

        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            return ResponseUtils.badRequest("Este evento não pertence ao organizador informado");
        }

        if ("ativar".equalsIgnoreCase(status)) {
            evento.setAtivo(true);
            repositorio.salvarEvento(evento);
            return ResponseUtils.ok("Evento ativado com sucesso!");
        } else if ("desativar".equalsIgnoreCase(status)) {
            evento.setAtivo(false);
            repositorio.salvarEvento(evento);

            if (!evento.getIngressos().isEmpty()) {
                repositorio.cancelarIngressosDoEvento(eventoId);
                return ResponseUtils.ok("Evento desativado com sucesso! " +
                        evento.getIngressos().size() + " ingressos cancelados e reembolsados.");
            }

            return ResponseUtils.ok("Evento desativado com sucesso!");
        } else {
            return ResponseUtils.badRequest("Status inválido. Use 'ativar' ou 'desativar'");
        }
    }

    @GetMapping(path = "/{organizadorId}/eventos")
    public ResponseEntity<?> listarEventosDoOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.notFound("Organizador não encontrado com ID: " + organizadorId);
        }

        List<EventoResponseDTO> eventos = repositorio.listarEventosDoOrganizador(organizadorId)
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.ok(eventos);
    }

    private String validarEventoRequest(EventoRequestDTO request) {
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return "Nome do evento é obrigatório";
        }
        if (request.getDataInicio() == null) {
            return "Data de início é obrigatória";
        }
        if (request.getDataFim() == null) {
            return "Data de fim é obrigatória";
        }
        if (request.getTipoEvento() == null) {
            return "Tipo de evento é obrigatório";
        }
        if (request.getModalidade() == null) {
            return "Modalidade é obrigatória";
        }
        if (request.getCapacidadeMaxima() == null || request.getCapacidadeMaxima() <= 0) {
            return "Capacidade máxima deve ser maior que zero";
        }
        if (request.getPrecoUnitarioIngresso() == null) {
            return "Preço do ingresso é obrigatório";
        }

        return validarDatasEvento(request.getDataInicio(), request.getDataFim());
    }

    private String validarDatasEvento(LocalDateTime dataInicio, LocalDateTime dataFim) {
        LocalDateTime agora = LocalDateTime.now();

        if (dataInicio.isBefore(agora)) {
            return "A data de início do evento não pode ser anterior à data atual";
        }

        if (dataFim.isBefore(dataInicio)) {
            return "A data de fim do evento não pode ser anterior à data de início";
        }

        Duration duracao = Duration.between(dataInicio, dataFim);
        if (duracao.toMinutes() < 30) {
            return "O evento deve ter no mínimo 30 minutos de duração";
        }

        return null;
    }
}