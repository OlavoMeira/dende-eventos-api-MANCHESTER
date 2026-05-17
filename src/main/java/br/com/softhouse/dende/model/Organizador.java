package br.com.softhouse.dende.model;

import java.time.LocalDate;
import java.util.Objects;

public class Organizador extends Usuario {

    private Empresa empresa;

    public Organizador() {
        super();
    }


    public Organizador(String nome, LocalDate dataNascimento, String sexo, String email,
                       String senha, String cnpj, String razaoSocial, String nomeFantasia) {
        super(nome, dataNascimento, sexo, email, senha);
        setTipoUsuario("ORGANIZADOR");

        if (cnpj != null && !cnpj.trim().isEmpty()) {
            this.empresa = new Empresa(cnpj, razaoSocial, nomeFantasia, null);
        }
    }

    public boolean isEmpresa() {
        return this.empresa != null && this.empresa.getCnpj() != null;
    }

    public Empresa getEmpresa() {
        return empresa;
    }
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }


    public String getCnpj() {
        return (empresa != null) ? empresa.getCnpj() : null;
    }

    public String getRazaoSocial() {
        return (empresa != null) ? empresa.getRazaoSocial() : null;
    }

    public String getNomeFantasia() {
        return (empresa != null) ? empresa.getNomeFantasia() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organizador that = (Organizador) o;
        return Objects.equals(getEmail(), that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }

    @Override
    public String toString() {
        return "Organizador{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", empresa=" + (isEmpresa() ? empresa.getCnpj() : "N/A") +
                '}';
    }
}