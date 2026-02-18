package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.model.*;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class IngressoController {

    private final Repositorio repositorio;

    public IngressoController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping(path = "/organizadores/{organizadorId}/eventos/{eventoId}/ingressos")
    public ResponseEntity<String> comprarIngresso(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody Map<String, Object> requestBody) {

        // Extrair dados do request body
        String emailUsuario = (String) requestBody.get("usuario");
        Double totalPago = requestBody.get("totalPago") != null ?
                Double.valueOf(requestBody.get("totalPago").toString()) : null;

        if (emailUsuario == null || emailUsuario.isEmpty()) {
            return ResponseUtils.badRequest("Email do usuário é obrigatório");
        }
        if (totalPago == null || totalPago <= 0) {
            return ResponseUtils.badRequest("Valor pago é obrigatório e deve ser maior que zero");
        }

        // Buscar evento
        Optional<Evento> eventoOpt = repositorio.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent()) {
            return ResponseUtils.badRequest("Evento não encontrado com ID: " + eventoId);
        }

        Evento evento = eventoOpt.get();

        // Verificar se o evento pertence ao organizador
        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            return ResponseUtils.badRequest("Este evento não pertence ao organizador informado");
        }

        // Verificar se o evento está ativo
        if (!evento.isAtivo()) {
            return ResponseUtils.badRequest("Este evento não está ativo para venda de ingressos");
        }

        // Verificar se ainda há vagas
        if (evento.isLotado()) {
            return ResponseUtils.badRequest("Este evento está lotado");
        }

        // Buscar usuário pelo email
        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorEmail(emailUsuario);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com email: " + emailUsuario);
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isAtivo()) {
            return ResponseUtils.badRequest("Usuário está desativado. Não é possível comprar ingressos.");
        }

        // Se o evento tiver evento principal, validar valor total
        if (evento.getEventoPrincipal() != null) {
            Evento eventoPrincipal = evento.getEventoPrincipal();
            Double valorTotal = eventoPrincipal.getPrecoUnitarioIngresso() + evento.getPrecoUnitarioIngresso();

            if (Math.abs(valorTotal - totalPago) > 0.01) {
                return ResponseUtils.badRequest("Valor pago incorreto. O valor total deve ser: " + valorTotal);
            }

            // Criar ingresso para o evento principal
            Ingresso ingressoPrincipal = new Ingresso(usuario, eventoPrincipal, eventoPrincipal.getPrecoUnitarioIngresso());
            repositorio.salvarIngresso(ingressoPrincipal);
        } else {
            // Validar valor pago para evento sem principal
            if (Math.abs(evento.getPrecoUnitarioIngresso() - totalPago) > 0.01) {
                return ResponseUtils.badRequest("Valor pago incorreto. O valor do ingresso é: " + evento.getPrecoUnitarioIngresso());
            }
        }

        // Criar ingresso para o evento atual
        Ingresso ingresso = new Ingresso(usuario, evento, evento.getPrecoUnitarioIngresso());
        repositorio.salvarIngresso(ingresso);

        if (evento.getEventoPrincipal() != null) {
            return ResponseUtils.ok("Ingressos comprados com sucesso! Foram gerados 2 ingressos (evento principal e sub-evento).");
        } else {
            return ResponseUtils.ok("Ingresso comprado com sucesso! ID: " + ingresso.getId());
        }
    }

    @PostMapping(path = "/usuarios/{usuarioId}/ingressos/{ingressoId}")
    public ResponseEntity<String> cancelarIngresso(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @PathVariable(parameter = "ingressoId") long ingressoId) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);
        }

        Optional<Ingresso> ingressoOpt = repositorio.buscarIngressoPorId(ingressoId);

        if (!ingressoOpt.isPresent()) {
            return ResponseUtils.badRequest("Ingresso não encontrado com ID: " + ingressoId);
        }

        Ingresso ingresso = ingressoOpt.get();

        // Verificar se o ingresso pertence ao usuário
        if (!ingresso.getUsuario().getId().equals(usuarioId)) {
            return ResponseUtils.badRequest("Este ingresso não pertence ao usuário informado");
        }

        // Verificar se o ingresso já não está cancelado
        if (ingresso.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.CANCELADO) {
            return ResponseUtils.badRequest("Este ingresso já está cancelado");
        }

        Evento evento = ingresso.getEvento();

        // Cancelar ingresso
        ingresso.cancelar(evento.getTaxaCancelamento());
        repositorio.salvarIngresso(ingresso);

        return ResponseUtils.ok("Ingresso cancelado com sucesso! Valor reembolsado: " + ingresso.getValorReembolsado());
    }

    @GetMapping(path = "/usuarios/{usuarioId}/ingressos")
    public ResponseEntity<Object> listarIngressosDoUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent()) {
            // Retorna um Map com erro em vez de String
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Usuário não encontrado com ID: " + usuarioId);
            return ResponseUtils.ok(erro); // Agora retorna Object
        }

        List<Map<String, Object>> ingressos = repositorio.listarIngressosDoUsuario(usuarioId)
                .stream()
                .map(ingresso -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", ingresso.getId());
                    map.put("evento", ingresso.getEvento().getNome());
                    map.put("dataEvento", ingresso.getEvento().getDataInicio().toString());
                    map.put("dataCompra", ingresso.getDataCompra().toString());
                    map.put("valorPago", ingresso.getValorPago());
                    map.put("status", ingresso.getStatus());

                    if (ingresso.getDataCancelamento() != null) {
                        map.put("dataCancelamento", ingresso.getDataCancelamento().toString());
                    }
                    if (ingresso.getValorReembolsado() != null) {
                        map.put("valorReembolsado", ingresso.getValorReembolsado());
                    }

                    map.put("eventoAtivo", ingresso.getEvento().isAtivo());
                    map.put("eventoFinalizado", ingresso.getEvento().isFinalizado());

                    return map;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseUtils.ok(ingressos);
    }
}