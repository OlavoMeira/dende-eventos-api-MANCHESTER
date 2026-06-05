package com.dende.eventos.service;

import com.dende.eventos.dto.EventoDTO;
import com.dende.eventos.exception.RegraDeNegocioException;
import com.dende.eventos.exception.ResourceNotFoundException;
import com.dende.eventos.model.Evento;
import com.dende.eventos.model.StatusEvento;
import com.dende.eventos.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public List<EventoDTO.Response> listarTodos() {
        return eventoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoDTO.Response buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EventoDTO.Response> listarPorStatus(StatusEvento status) {
        return eventoRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoDTO.Response> listarComVagas() {
        return eventoRepository.findEventosComVagasDisponiveis(StatusEvento.ATIVO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EventoDTO.Response criar(EventoDTO.Request dto) {
        validarDatas(dto.getDataInicio(), dto.getDataFim());

        if (eventoRepository.existsByNomeAndDataInicio(dto.getNome(), dto.getDataInicio())) {
            throw new RegraDeNegocioException(
                    "Já existe um evento com este nome nesta data/horário.");
        }

        Evento evento = Evento.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .local(dto.getLocal())
                .capacidadeMaxima(dto.getCapacidadeMaxima())
                .status(StatusEvento.ATIVO)
                .build();

        return toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoDTO.Response atualizar(Long id, EventoDTO.Request dto) {
        Evento evento = findOrThrow(id);

        if (evento.getStatus() == StatusEvento.CANCELADO) {
            throw new RegraDeNegocioException("Não é possível editar um evento cancelado.");
        }

        validarDatas(dto.getDataInicio(), dto.getDataFim());

        evento.setNome(dto.getNome());
        evento.setDescricao(dto.getDescricao());
        evento.setDataInicio(dto.getDataInicio());
        evento.setDataFim(dto.getDataFim());
        evento.setLocal(dto.getLocal());

        if (dto.getCapacidadeMaxima() < evento.getParticipantes().size()) {
            throw new RegraDeNegocioException(
                    "Nova capacidade não pode ser menor que o número de participantes inscritos.");
        }
        evento.setCapacidadeMaxima(dto.getCapacidadeMaxima());

        return toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void cancelar(Long id) {
        Evento evento = findOrThrow(id);

        if (evento.getStatus() == StatusEvento.CANCELADO) {
            throw new RegraDeNegocioException("Evento já está cancelado.");
        }
        if (evento.getStatus() == StatusEvento.ENCERRADO) {
            throw new RegraDeNegocioException("Não é possível cancelar um evento encerrado.");
        }

        evento.setStatus(StatusEvento.CANCELADO);
        eventoRepository.save(evento);
    }

    @Transactional
    public void deletar(Long id) {
        Evento evento = findOrThrow(id);
        if (!evento.getParticipantes().isEmpty()) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir evento com participantes inscritos. Cancele-o primeiro.");
        }
        eventoRepository.delete(evento);
    }


    public Evento findOrThrow(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com id: " + id));
    }

    private void validarDatas(LocalDateTime inicio, LocalDateTime fim) {
        if (fim.isBefore(inicio) || fim.isEqual(inicio)) {
            throw new RegraDeNegocioException(
                    "A data de fim deve ser posterior à data de início.");
        }
    }

    public EventoDTO.Response toResponse(Evento e) {
        return EventoDTO.Response.builder()
                .id(e.getId())
                .nome(e.getNome())
                .descricao(e.getDescricao())
                .dataInicio(e.getDataInicio())
                .dataFim(e.getDataFim())
                .local(e.getLocal())
                .capacidadeMaxima(e.getCapacidadeMaxima())
                .totalParticipantes(e.getParticipantes().size())
                .status(e.getStatus())
                .criadoEm(e.getCriadoEm())
                .atualizadoEm(e.getAtualizadoEm())
                .build();
    }
}
