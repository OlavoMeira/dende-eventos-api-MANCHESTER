package br.com.softhouse.dende.model;

import br.com.softhouse.dende.enums.StatusIngresso;

import java.time.LocalDateTime;
import java.util.Objects;

public class Ingresso {
    private Long id;
    private Usuario usuario;
    private Evento evento;
    private LocalDateTime dataCompra;
    private LocalDateTime dataCancelamento;
    private Double valorPago;
    private Double valorReembolsado;
    private StatusIngresso status;

    public Ingresso() {
        this.dataCompra = LocalDateTime.now();
        this.status = StatusIngresso.ATIVO;
    }

    public Ingresso(Usuario usuario, Evento evento, Double valorPago) {
        this.usuario = usuario;
        this.evento = evento;
        this.valorPago = valorPago;
        this.dataCompra = LocalDateTime.now();
        this.status = StatusIngresso.ATIVO;
    }

    public void cancelar(Double taxaCancelamento) {
        this.status = StatusIngresso.CANCELADO;
        this.dataCancelamento = LocalDateTime.now();

        if (taxaCancelamento > 0) {
            this.valorReembolsado = valorPago * (1 - taxaCancelamento);
        } else {
            this.valorReembolsado = valorPago;
        }
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Evento getEvento() {
        return evento;
    }
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public LocalDateTime getDataCompra() {
        return dataCompra;
    }
    public void setDataCompra(LocalDateTime dataCompra) {
        this.dataCompra = dataCompra;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }
    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public Double getValorPago() {
        return valorPago;
    }
    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
    }

    public Double getValorReembolsado() {
        return valorReembolsado;
    }
    public void setValorReembolsado(Double valorReembolsado) {
        this.valorReembolsado = valorReembolsado;
    }

    public StatusIngresso getStatus() {
        return status;
    }
    public void setStatus(StatusIngresso status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingresso ingresso = (Ingresso) o;
        return Objects.equals(id, ingresso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ingresso{" +
                "id=" + id +
                ", evento=" + evento.getNome() +
                ", status=" + status +
                '}';
    }
}