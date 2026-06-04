package com.dende.eventos.service;

import com.dende.eventos.dto.ParticipanteDTO;
import com.dende.eventos.exception.RegraDeNegocioException;
import com.dende.eventos.exception.ResourceNotFoundException;
import com.dende.eventos.model.Evento;
import com.dende.eventos.model.Participante;
import com.dende.eventos.model.StatusEvento;
import com.dende.eventos.repository.EventoRepository;
import com.dende.eventos.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final EventoRepository eventoRepository;
    private final EventoService eventoService;

    @Transactional(readOnly = true)
    public List<ParticipanteDTO.Response> listarPorEvento(Long eventoId) {
        eventoService.findOrThrow(eventoId); // valida que evento existe
        return participanteRepository.findByEventoId(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParticipanteDTO.Response buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ParticipanteDTO.Response inscrever(Long eventoId, ParticipanteDTO.Request dto) {
        Evento evento = eventoService.findOrThrow(eventoId);

        if (evento.getStatus() != StatusEvento.ATIVO) {
            throw new RegraDeNegocioException(
                    "Inscrições não estão abertas para este evento. Status: " + evento.getStatus());
        }

        if (evento.getParticipantes().size() >= evento.getCapacidadeMaxima()) {
            // Lança exceção sem tentar persistir — a lógica de marcar ESGOTADO
            // deve ocorrer DENTRO de uma operação bem-sucedida (última vaga preenchida),
            // não num caminho de erro. Aqui apenas informamos que está esgotado.
            throw new RegraDeNegocioException("Evento esgotado. Não há vagas disponíveis.");
        }

        if (participanteRepository.existsByEmailAndEventoId(dto.getEmail(), eventoId)) {
            throw new RegraDeNegocioException("Este e-mail já está inscrito neste evento.");
        }

        if (participanteRepository.existsByCpfAndEventoId(dto.getCpf(), eventoId)) {
            throw new RegraDeNegocioException("Este CPF já está inscrito neste evento.");
        }

        Participante participante = Participante.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .evento(evento)
                .build();

        ParticipanteDTO.Response response = toResponse(participanteRepository.save(participante));

        // Verifica se o evento atingiu a capacidade máxima após esta inscrição
        long totalInscritos = participanteRepository.findByEventoId(eventoId).size();
        if (totalInscritos >= evento.getCapacidadeMaxima()) {
            evento.setStatus(StatusEvento.ESGOTADO);
            eventoRepository.save(evento);
        }

        return response;
    }

    @Transactional
    public void cancelarInscricao(Long id) {
        Participante participante = findOrThrow(id);
        Evento evento = participante.getEvento();

        if (evento.getStatus() == StatusEvento.ENCERRADO) {
            throw new RegraDeNegocioException(
                    "Não é possível cancelar inscrição de um evento encerrado.");
        }

        participanteRepository.delete(participante);

        // Reabre vagas se o evento estava esgotado e agora há espaço
        if (evento.getStatus() == StatusEvento.ESGOTADO) {
            evento.setStatus(StatusEvento.ATIVO);
            eventoRepository.save(evento);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Participante findOrThrow(Long id) {
        return participanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participante não encontrado com id: " + id));
    }

    private ParticipanteDTO.Response toResponse(Participante p) {
        return ParticipanteDTO.Response.builder()
                .id(p.getId())
                .nome(p.getNome())
                .email(p.getEmail())
                .cpf(p.getCpf())
                .eventoId(p.getEvento().getId())
                .eventoNome(p.getEvento().getNome())
                .dataInscricao(p.getDataInscricao())
                .build();
    }
}
