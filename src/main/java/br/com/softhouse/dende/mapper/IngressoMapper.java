package br.com.softhouse.dende.mapper;

import br.com.softhouse.dende.dto.response.IngressoResponseDTO;
import br.com.softhouse.dende.model.Ingresso;

public class IngressoMapper {

    private IngressoMapper() {

    }

    
    public static IngressoResponseDTO toResponse(Ingresso ingresso) {
        if (ingresso == null) return null;

        IngressoResponseDTO dto = new IngressoResponseDTO();
        dto.setId(ingresso.getId());
        dto.setValorPago(ingresso.getValorPago());
        dto.setValorReembolsado(ingresso.getValorReembolsado());
        dto.setStatus(ingresso.getStatus());

        dto.setDataCompra(ingresso.getDataCompra() != null
                ? ingresso.getDataCompra().toString() : null);
        dto.setDataCancelamento(ingresso.getDataCancelamento() != null
                ? ingresso.getDataCancelamento().toString() : null);

        if (ingresso.getEvento() != null) {
            dto.setEvento(ingresso.getEvento().getNome());
            dto.setDataEvento(ingresso.getEvento().getDataInicio() != null
                    ? ingresso.getEvento().getDataInicio().toString() : null);
            dto.setEventoAtivo(ingresso.getEvento().isAtivo());
            dto.setEventoFinalizado(ingresso.getEvento().isFinalizado());
        }

        return dto;
    }
}
