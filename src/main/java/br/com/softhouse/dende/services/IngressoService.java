package br.com.softhouse.dende.services;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.dto.request.IngressoRequestDTO;
import br.com.softhouse.dende.dto.response.IngressoResponseDTO;
import br.com.softhouse.dende.exceptions.*;
import br.com.softhouse.dende.mapper.IngressoMapper;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Ingresso;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.EventoRepository;
import br.com.softhouse.dende.repositories.IngressoRepository;
import br.com.softhouse.dende.repositories.UsuarioRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IngressoService {

    private final IngressoRepository ingressoRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public IngressoService(IngressoRepository ingressoRepository,
                           EventoRepository eventoRepository,
                           UsuarioRepository usuarioRepository) {
        this.ingressoRepository = ingressoRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public IngressoResponseDTO comprar(long organizadorId, long eventoId, IngressoRequestDTO dto) {
        if (dto.getUsuario() == null || dto.getUsuario().trim().isEmpty())
            throw new RegraDeNegocioException("Email do usuário é obrigatório.");
        if (dto.getTotalPago() == null || dto.getTotalPago() <= 0)
            throw new RegraDeNegocioException("Valor pago é obrigatório e deve ser maior que zero.");

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", eventoId));

        if (!evento.getOrganizador().getId().equals(organizadorId))
            throw new RegraDeNegocioException("Este evento não pertence ao organizador informado.");
        if (!evento.isAtivo())
            throw new RegraDeNegocioException("Este evento não está ativo para venda de ingressos.");

        int ingressosAtivos = ingressoRepository.contarIngressosAtivos(eventoId);
        if (evento.getCapacidadeMaxima() != null && ingressosAtivos >= evento.getCapacidadeMaxima())
            throw new EventoLotadoException(evento.getNome());

        Usuario usuario = usuarioRepository.findByEmail(dto.getUsuario())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário com e-mail " + dto.getUsuario() + " não encontrado."));

        if (!usuario.isAtivo())
            throw new RegraDeNegocioException("Usuário está desativado. Não é possível comprar ingressos.");

        // Sub-evento: valida valor total e emite ingresso para o evento principal também
        if (evento.getEventoPrincipal() != null) {
            Evento principal = evento.getEventoPrincipal();
            double valorTotal = principal.getPrecoUnitarioIngresso() + evento.getPrecoUnitarioIngresso();

            if (Math.abs(valorTotal - dto.getTotalPago()) > 0.01)
                throw new RegraDeNegocioException(
                        "Valor pago incorreto. O valor total deve ser: " + valorTotal);

            Ingresso ingressoPrincipal = new Ingresso(usuario, principal, principal.getPrecoUnitarioIngresso());
            ingressoRepository.save(ingressoPrincipal);
        }

        Ingresso ingresso = new Ingresso(usuario, evento, evento.getPrecoUnitarioIngresso());
        ingressoRepository.save(ingresso);
        return IngressoMapper.toResponse(ingresso);
    }

    public IngressoResponseDTO cancelar(long usuarioId, long ingressoId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));

        Ingresso ingresso = ingressoRepository.findById(ingressoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ingresso", ingressoId));

        if (!ingresso.getUsuario().getId().equals(usuarioId))
            throw new RegraDeNegocioException("Este ingresso não pertence ao usuário informado.");

        // cancelar() já valida status CANCELADO e lança IngressoJaCanceladoException
        double taxa = ingresso.getEvento().getTaxaCancelamento();
        Ingresso cancelado = ingressoRepository.cancelar(ingressoId, taxa);
        return IngressoMapper.toResponse(cancelado);
    }

    public List<IngressoResponseDTO> listarDoUsuario(long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));

        return ingressoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(IngressoMapper::toResponse)
                .collect(Collectors.toList());
    }
}