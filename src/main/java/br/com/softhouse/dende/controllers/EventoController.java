package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.mapper.EventoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
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
            @RequestBody EventoRequestDTO dto) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizador = organizadorOpt.get();

        if (!organizador.isAtivo()) {
            return ResponseUtils.badRequest("Organizador está desativado. Não é possível cadastrar eventos.");
        }

        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome do evento é obrigatório");
        }
        if (dto.getDataInicio() == null) {
            return ResponseUtils.badRequest("Data de início é obrigatória");
        }
        if (dto.getDataFim() == null) {
            return ResponseUtils.badRequest("Data de fim é obrigatória");
        }
        if (dto.getTipoEvento() == null) {
            return ResponseUtils.badRequest("Tipo de evento é obrigatório");
        }
        if (dto.getModalidade() == null) {
            return ResponseUtils.badRequest("Modalidade é obrigatória");
        }
        if (dto.getCapacidadeMaxima() == null || dto.getCapacidadeMaxima() <= 0) {
            return ResponseUtils.badRequest("Capacidade máxima deve ser maior que zero");
        }
        if (dto.getPrecoUnitarioIngresso() == null) {
            return ResponseUtils.badRequest("Preço do ingresso é obrigatório");
        }

        LocalDateTime agora = LocalDateTime.now();

        if (dto.getDataInicio().isBefore(agora)) {
            return ResponseUtils.badRequest("A data de início do evento não pode ser anterior à data atual");
        }
        if (dto.getDataFim().isBefore(dto.getDataInicio())) {
            return ResponseUtils.badRequest("A data de fim do evento não pode ser anterior à data de início");
        }

        Duration duracao = Duration.between(dto.getDataInicio(), dto.getDataFim());
        if (duracao.toMinutes() < 30) {
            return ResponseUtils.badRequest("O evento deve ter no mínimo 30 minutos de duração");
        }

        Evento evento = EventoMapper.toModel(dto);
        evento.setOrganizador(organizador);
        evento.setAtivo(true);

        if (dto.getEventoPrincipalId() != null) {
            Optional<Evento> eventoPrincipalOpt = repositorio.buscarEventoPorId(dto.getEventoPrincipalId());
            if (!eventoPrincipalOpt.isPresent()) {
                return ResponseUtils.badRequest("Evento principal não encontrado com ID: " + dto.getEventoPrincipalId());
            }
            evento.setEventoPrincipal(eventoPrincipalOpt.get());
        }

        repositorio.salvarEvento(evento);

        return ResponseUtils.ok("Evento " + evento.getNome() + " cadastrado com sucesso! ID: " + evento.getId());
    }

    @PutMapping(path = "/{organizadorId}/eventos/{eventoId}")
    public ResponseEntity<String> alterarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody EventoRequestDTO dto) {

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

        if (dto.getDataInicio() != null && dto.getDataFim() != null) {
            LocalDateTime agora = LocalDateTime.now();

            if (dto.getDataInicio().isBefore(agora)) {
                return ResponseUtils.badRequest("A data de início do evento não pode ser anterior à data atual");
            }
            if (dto.getDataFim().isBefore(dto.getDataInicio())) {
                return ResponseUtils.badRequest("A data de fim do evento não pode ser anterior à data de início");
            }

            Duration duracao = Duration.between(dto.getDataInicio(), dto.getDataFim());
            if (duracao.toMinutes() < 30) {
                return ResponseUtils.badRequest("O evento deve ter no mínimo 30 minutos de duração");
            }
        }

        EventoMapper.updateModel(dto, eventoExistente);
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
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        List<EventoResponseDTO> eventos = repositorio.listarEventosDoOrganizador(organizadorId)
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.ok(eventos);
    }
}
