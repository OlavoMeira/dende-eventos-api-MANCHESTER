package br.com.softhouse.dende.dto.response;

import br.com.softhouse.dende.enums.StatusIngresso;

public class IngressoResponseDTO {

    private Long id;
    private String evento;
    private String dataEvento;
    private String dataCompra;
    private String dataCancelamento;
    private Double valorPago;
    private Double valorReembolsado;
    private StatusIngresso status;
    private boolean eventoAtivo;
    private boolean eventoFinalizado;

    public IngressoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getDataEvento() { return dataEvento; }
    public void setDataEvento(String dataEvento) { this.dataEvento = dataEvento; }

    public String getDataCompra() { return dataCompra; }
    public void setDataCompra(String dataCompra) { this.dataCompra = dataCompra; }

    public String getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(String dataCancelamento) { this.dataCancelamento = dataCancelamento; }

    public Double getValorPago() { return valorPago; }
    public void setValorPago(Double valorPago) { this.valorPago = valorPago; }

    public Double getValorReembolsado() { return valorReembolsado; }
    public void setValorReembolsado(Double valorReembolsado) { this.valorReembolsado = valorReembolsado; }

    public StatusIngresso getStatus() { return status; }
    public void setStatus(StatusIngresso status) { this.status = status; }

    public boolean isEventoAtivo() { return eventoAtivo; }
    public void setEventoAtivo(boolean eventoAtivo) { this.eventoAtivo = eventoAtivo; }

    public boolean isEventoFinalizado() { return eventoFinalizado; }
    public void setEventoFinalizado(boolean eventoFinalizado) { this.eventoFinalizado = eventoFinalizado; }
}
