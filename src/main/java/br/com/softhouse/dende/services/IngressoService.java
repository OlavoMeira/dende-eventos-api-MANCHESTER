package br.com.softhouse.dende.services;

import br.com.softhouse.dende.dto.request.IngressoRequestDTO;
import br.com.softhouse.dende.dto.response.IngressoResponseDTO;
import br.com.softhouse.dende.mapper.IngressoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Ingresso;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.Repositorio;

import java.util.List;
import java.util.stream.Collectors;

public class IngressoService {

    private final Repositorio repositorio;

    public IngressoService() {
        this.repositorio = Repositorio.getInstance();
    }

    public IngressoResponseDTO comprar(long organizadorId, long eventoId, IngressoRequestDTO dto) {
        if (dto.getUsuario() == null || dto.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Email do usuário é obrigatório");
        }
        if (dto.getTotalPago() == null || dto.getTotalPago() <= 0) {
            throw new IllegalArgumentException("Valor pago é obrigatório e deve ser maior que zero");
        }

        Evento evento = repositorio.buscarEventoPorId(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com ID: " + eventoId));

        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            throw new IllegalArgumentException("Este evento não pertence ao organizador informado");
        }
        if (!evento.isAtivo()) {
            throw new IllegalArgumentException("Este evento não está ativo para venda de ingressos");
        }
        if (evento.isLotado()) {
            throw new IllegalArgumentException("Este evento está lotado");
        }

        Usuario usuario = repositorio.buscarUsuarioPorEmail(dto.getUsuario())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado com email: " + dto.getUsuario()));

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário está desativado. Não é possível comprar ingressos.");
        }

        if (evento.getEventoPrincipal() != null) {
            Evento eventoPrincipal = evento.getEventoPrincipal();
            Double valorTotal = eventoPrincipal.getPrecoUnitarioIngresso() + evento.getPrecoUnitarioIngresso();

            if (Math.abs(valorTotal - dto.getTotalPago()) > 0.01) {
                throw new IllegalArgumentException(
                        "Valor pago incorreto. O valor total deve ser: " + valorTotal);
            }

            Ingresso ingressoPrincipal = new Ingresso(usuario, eventoPrincipal, eventoPrincipal.getPrecoUnitarioIngresso());
            repositorio.salvarIngresso(ingressoPrincipal);
        }

        Ingresso ingresso = new Ingresso(usuario, evento, evento.getPrecoUnitarioIngresso());
        repositorio.salvarIngresso(ingresso);

        return IngressoMapper.toResponse(ingresso);
    }

    public IngressoResponseDTO cancelar(long usuarioId, long ingressoId) {
        repositorio.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        Ingresso ingresso = repositorio.buscarIngressoPorId(ingressoId)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado com ID: " + ingressoId));

        if (!ingresso.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Este ingresso não pertence ao usuário informado");
        }
        if (ingresso.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.CANCELADO) {
            throw new IllegalArgumentException("Este ingresso já está cancelado");
        }

        Evento evento = ingresso.getEvento();
        ingresso.cancelar(evento.getTaxaCancelamento());
        repositorio.salvarIngresso(ingresso);

        return IngressoMapper.toResponse(ingresso);
    }

    public List<IngressoResponseDTO> listarDoUsuario(long usuarioId) {
        repositorio.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        return repositorio.listarIngressosDoUsuario(usuarioId)
                .stream()
                .map(IngressoMapper::toResponse)
                .collect(Collectors.toList());
    }
}