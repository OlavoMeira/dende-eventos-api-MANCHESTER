package br.com.softhouse.dende.mapper;

import br.com.softhouse.dende.dto.request.EventoRequestDTO;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.model.Evento;

public class EventoMapper {

    private EventoMapper() {

    }

    
    public static Evento toModel(EventoRequestDTO dto) {
        if (dto == null) return null;

        Evento evento = new Evento();
        evento.setNome(dto.getNome());
        evento.setPaginaWeb(dto.getPaginaWeb());
        evento.setDescricao(dto.getDescricao());
        evento.setDataInicio(dto.getDataInicio());
        evento.setDataFim(dto.getDataFim());
        evento.setTipoEvento(dto.getTipoEvento());
        evento.setModalidade(dto.getModalidade());
        evento.setLocal(dto.getLocal());
        evento.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        evento.setPrecoUnitarioIngresso(dto.getPrecoUnitarioIngresso());
        evento.setTaxaCancelamento(dto.getTaxaCancelamento());
        return evento;
    }

    
    public static void updateModel(EventoRequestDTO dto, Evento eventoExistente) {
        if (dto == null || eventoExistente == null) return;

        eventoExistente.setNome(dto.getNome());
        eventoExistente.setPaginaWeb(dto.getPaginaWeb());
        eventoExistente.setDescricao(dto.getDescricao());
        eventoExistente.setDataInicio(dto.getDataInicio());
        eventoExistente.setDataFim(dto.getDataFim());
        eventoExistente.setTipoEvento(dto.getTipoEvento());
        eventoExistente.setModalidade(dto.getModalidade());
        eventoExistente.setLocal(dto.getLocal());
        eventoExistente.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        eventoExistente.setPrecoUnitarioIngresso(dto.getPrecoUnitarioIngresso());
        eventoExistente.setTaxaCancelamento(dto.getTaxaCancelamento());

    }

    
    public static EventoResponseDTO toResponse(Evento evento) {
        if (evento == null) return null;

        EventoResponseDTO dto = new EventoResponseDTO();
        dto.setId(evento.getId());
        dto.setNome(evento.getNome());
        dto.setPaginaWeb(evento.getPaginaWeb());
        dto.setDescricao(evento.getDescricao());
        dto.setDataInicio(evento.getDataInicio() != null
                ? evento.getDataInicio().toString() : null);
        dto.setDataFim(evento.getDataFim() != null
                ? evento.getDataFim().toString() : null);
        dto.setTipoEvento(evento.getTipoEvento());
        dto.setModalidade(evento.getModalidade());
        dto.setLocal(evento.getLocal());
        dto.setCapacidadeMaxima(evento.getCapacidadeMaxima());
        dto.setPrecoUnitarioIngresso(evento.getPrecoUnitarioIngresso());
        dto.setTaxaCancelamento(evento.getTaxaCancelamento());
        dto.setAtivo(evento.isAtivo());
        dto.setIngressosVendidos(evento.getIngressosVendidos());
        dto.setVagasDisponiveis(evento.getVagasDisponiveis());

        if (evento.getOrganizador() != null) {
            dto.setOrganizador(evento.getOrganizador().getNome());
        }

        if (evento.getEventoPrincipal() != null) {
            dto.setEventoPrincipalId(evento.getEventoPrincipal().getId());
            dto.setEventoPrincipalNome(evento.getEventoPrincipal().getNome());
        }

        return dto;
    }
}
