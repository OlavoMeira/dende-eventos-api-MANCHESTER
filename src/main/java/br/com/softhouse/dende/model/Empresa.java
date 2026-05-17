package br.com.softhouse.dende.model;

import java.util.Objects;

public class Empresa {

    private Long id;
    private Long organizadorId;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;

    public Empresa(String cnpj, String razaoSocial, String nomeFantasia, Long organizadorId) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.organizadorId = organizadorId;
    }

    public Empresa() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizadorId() {
        return organizadorId;
    }
    public void setOrganizadorId(Long organizadorId) {
        this.organizadorId = organizadorId;
    }

    public String getCnpj() {
        return cnpj;
    }
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }
    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }
    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empresa)) return false;
        Empresa empresa = (Empresa) o;
        return Objects.equals(cnpj, empresa.cnpj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnpj);
    }
}
