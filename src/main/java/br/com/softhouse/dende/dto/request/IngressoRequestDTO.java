package br.com.softhouse.dende.dto.request;

public class IngressoRequestDTO {

    private String usuario;
    private Double totalPago;

    public IngressoRequestDTO() {}

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Double getTotalPago() { return totalPago; }
    public void setTotalPago(Double totalPago) { this.totalPago = totalPago; }
}
