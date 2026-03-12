package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.IngressoRequestDTO;
import br.com.softhouse.dende.dto.response.IngressoResponseDTO;
import br.com.softhouse.dende.mapper.IngressoMapper;
import br.com.softhouse.dende.model.*;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class IngressoController {

    private final Repositorio repositorio;

    public IngressoController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping(path = "/organizadores/{organizadorId}/eventos/{eventoId}/ingressos")
    public ResponseEntity<?> comprarIngresso(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "eventoId") long eventoId,
            @RequestBody IngressoRequestDTO dto) {

        if (dto.getUsuario() == null || dto.getUsuario().isEmpty())
            return ResponseUtils.badRequest("Email do usuário é obrigatório");
        if (dto.getTotalPago() == null || dto.getTotalPago() <= 0)
            return ResponseUtils.badRequest("Valor pago é obrigatório e deve ser maior que zero");

        Optional<Evento> eventoOpt = repositorio.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent())
            return ResponseUtils.badRequest("Evento não encontrado com ID: " + eventoId);

        Evento evento = eventoOpt.get();

        if (!evento.getOrganizador().getId().equals(organizadorId))
            return ResponseUtils.badRequest("Este evento não pertence ao organizador informado");
        if (!evento.isAtivo())
            return ResponseUtils.badRequest("Este evento não está ativo para venda de ingressos");
        if (evento.isLotado())
            return ResponseUtils.badRequest("Este evento está lotado");

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorEmail(dto.getUsuario());

        if (!usuarioOpt.isPresent())
            return ResponseUtils.badRequest("Usuário não encontrado com email: " + dto.getUsuario());

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isAtivo())
            return ResponseUtils.badRequest("Usuário está desativado. Não é possível comprar ingressos.");

        if (evento.getEventoPrincipal() != null) {
            Evento eventoPrincipal = evento.getEventoPrincipal();
            Double valorTotal = eventoPrincipal.getPrecoUnitarioIngresso() + evento.getPrecoUnitarioIngresso();
            if (Math.abs(valorTotal - dto.getTotalPago()) > 0.01)
                return ResponseUtils.badRequest("Valor pago incorreto. O valor total deve ser: " + valorTotal);

            Ingresso ingressoPrincipal = new Ingresso(usuario, eventoPrincipal, eventoPrincipal.getPrecoUnitarioIngresso());
            repositorio.salvarIngresso(ingressoPrincipal);
        }

        Ingresso ingresso = new Ingresso(usuario, evento, evento.getPrecoUnitarioIngresso());
        repositorio.salvarIngresso(ingresso);

        if (evento.getEventoPrincipal() != null)
            return ResponseUtils.ok("Ingressos comprados com sucesso! Foram gerados 2 ingressos (evento principal e sub-evento).");

        return ResponseUtils.ok("Ingresso comprado com sucesso! ID: " + ingresso.getId());
    }

    @PostMapping(path = "/usuarios/{usuarioId}/ingressos/{ingressoId}")
    public ResponseEntity<?> cancelarIngresso(
            @PathVariable(parameter = "usuarioId") long usuarioId,
            @PathVariable(parameter = "ingressoId") long ingressoId) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent())
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);

        Optional<Ingresso> ingressoOpt = repositorio.buscarIngressoPorId(ingressoId);

        if (!ingressoOpt.isPresent())
            return ResponseUtils.badRequest("Ingresso não encontrado com ID: " + ingressoId);

        Ingresso ingresso = ingressoOpt.get();

        if (!ingresso.getUsuario().getId().equals(usuarioId))
            return ResponseUtils.badRequest("Este ingresso não pertence ao usuário informado");
        if (ingresso.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.CANCELADO)
            return ResponseUtils.badRequest("Este ingresso já está cancelado");

        ingresso.cancelar(ingresso.getEvento().getTaxaCancelamento());
        repositorio.salvarIngresso(ingresso);

        return ResponseUtils.ok("Ingresso cancelado com sucesso! Valor reembolsado: " + ingresso.getValorReembolsado());
    }

    @GetMapping(path = "/usuarios/{usuarioId}/ingressos")
    public ResponseEntity<?> listarIngressosDoUsuario(
            @PathVariable(parameter = "usuarioId") long usuarioId) {

        Optional<Usuario> usuarioOpt = repositorio.buscarUsuarioPorId(usuarioId);

        if (!usuarioOpt.isPresent())
            return ResponseUtils.badRequest("Usuário não encontrado com ID: " + usuarioId);

        List<IngressoResponseDTO> ingressos = repositorio.listarIngressosDoUsuario(usuarioId)
                .stream()
                .map(IngressoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.ok(ingressos);
    }
}