package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            @PathVariable(parameter = "organizadorId") long organizadorId,  // Agora vem do método
            @RequestBody Evento evento) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizador = organizadorOpt.get();

        if (!organizador.isAtivo()) {
            return ResponseUtils.badRequest("Organizador está desativado. Não é possível cadastrar eventos.");
        }

        // Validar campos obrigatórios
        if (evento.getNome() == null || evento.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome do evento é obrigatório");
        }
        if (evento.getDataInicio() == null) {
            return ResponseUtils.badRequest("Data de início é obrigatória");
        }
        if (evento.getDataFim() == null) {
            return ResponseUtils.badRequest("Data de fim é obrigatória");
        }
        if (evento.getTipoEvento() == null) {
            return ResponseUtils.badRequest("Tipo de evento é obrigatório");
        }
        if (evento.getModalidade() == null) {
            return ResponseUtils.badRequest("Modalidade é obrigatória");
        }
        if (evento.getCapacidadeMaxima() == null || evento.getCapacidadeMaxima() <= 0) {
            return ResponseUtils.badRequest("Capacidade máxima deve ser maior que zero");
        }
        if (evento.getPrecoUnitarioIngresso() == null) {
            return ResponseUtils.badRequest("Preço do ingresso é obrigatório");
        }


        // Validar datas do evento
        LocalDateTime agora = LocalDateTime.now();

        if (evento.getDataInicio().isBefore(agora)) {
            return ResponseUtils.badRequest("A data de início do evento não pode ser anterior à data atual");
        }

        if (evento.getDataFim().isBefore(evento.getDataInicio())) {
            return ResponseUtils.badRequest("A data de fim do evento não pode ser anterior à data de início");
        }

        Duration duracao = Duration.between(evento.getDataInicio(), evento.getDataFim());
        if (duracao.toMinutes() < 30) {
            return ResponseUtils.badRequest("O evento deve ter no mínimo 30 minutos de duração");
        }

        // Configurar evento
        evento.setOrganizador(organizador);
        evento.setAtivo(true);

        // Verificar se tem evento principal
        if (evento.getEventoPrincipal() != null && evento.getEventoPrincipal().getId() != null) {
            Optional<Evento> eventoPrincipalOpt = repositorio.buscarEventoPorId(evento.getEventoPrincipal().getId());
            if (eventoPrincipalOpt.isPresent()) {
                evento.setEventoPrincipal(eventoPrincipalOpt.get());
            }
        }

        repositorio.salvarEvento(evento);

        return ResponseUtils.ok("Evento " + evento.getNome() + " cadastrado com sucesso! ID: " + evento.getId());
    }

    @PutMapping(path = "/{organizadorId}/eventos/{eventoId}")
    public ResponseEntity<String> alterarEvento(
            @PathVariable(parameter = "organizadorId") long organizadorId,  // Vem do método
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody Evento evento) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Optional<Evento> eventoOpt = repositorio.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent()) {
            return ResponseUtils.badRequest("Evento não encontrado com ID: " + eventoId);
        }

        Evento eventoExistente = eventoOpt.get();

        // Verificar se o evento pertence ao organizador
        if (!eventoExistente.getOrganizador().getId().equals(organizadorId)) {
            return ResponseUtils.badRequest("Este evento não pertence ao organizador informado");
        }

        // Verificar se o evento está ativo para alteração
        if (!eventoExistente.isAtivo()) {
            return ResponseUtils.badRequest("Não é possível alterar um evento inativo");
        }

        // Manter o ID e o organizador
        evento.setId(eventoId);
        evento.setOrganizador(organizadorOpt.get());

        // Manter o status atual (não alterar ativação aqui)
        evento.setAtivo(eventoExistente.isAtivo());

        // Manter ingressos existentes
        evento.setIngressos(eventoExistente.getIngressos());

        // Validar datas se foram alteradas
        if (evento.getDataInicio() != null && evento.getDataFim() != null) {
            LocalDateTime agora = LocalDateTime.now();

            if (evento.getDataInicio().isBefore(agora)) {
                return ResponseUtils.badRequest("A data de início do evento não pode ser anterior à data atual");
            }

            if (evento.getDataFim().isBefore(evento.getDataInicio())) {
                return ResponseUtils.badRequest("A data de fim do evento não pode ser anterior à data de início");
            }

            Duration duracao = Duration.between(evento.getDataInicio(), evento.getDataFim());
            if (duracao.toMinutes() < 30) {
                return ResponseUtils.badRequest("O evento deve ter no mínimo 30 minutos de duração");
            }
        }

        repositorio.salvarEvento(evento);

        return ResponseUtils.ok("Evento " + evento.getNome() + " alterado com sucesso!");
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

        // Verificar se o evento pertence ao organizador
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

            // Se tiver ingressos vendidos, cancelar e reembolsar
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
    public ResponseEntity<Object> listarEventosDoOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            // Retorna um Map com erro em vez de String
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Organizador não encontrado com ID: " + organizadorId);
            return ResponseUtils.ok(erro); // Agora retorna Object
        }

        List<Map<String, Object>> eventos = repositorio.listarEventosDoOrganizador(organizadorId)
                .stream()
                .map(evento -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", evento.getId());
                    map.put("nome", evento.getNome());
                    map.put("dataInicio", evento.getDataInicio().toString());
                    map.put("dataFim", evento.getDataFim().toString());
                    map.put("local", evento.getLocal());
                    map.put("modalidade", evento.getModalidade());
                    map.put("precoIngresso", evento.getPrecoUnitarioIngresso());
                    map.put("capacidadeMaxima", evento.getCapacidadeMaxima());
                    map.put("ingressosVendidos", evento.getIngressosVendidos());
                    map.put("vagasDisponiveis", evento.getVagasDisponiveis());
                    map.put("ativo", evento.isAtivo());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseUtils.ok(eventos);
    }
}