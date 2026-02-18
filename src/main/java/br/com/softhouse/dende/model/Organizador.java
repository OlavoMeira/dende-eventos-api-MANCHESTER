package br.com.softhouse.dende.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class Organizador {
    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private String sexo;
    private String email;
    private String senha;
    private boolean ativo;

    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;

    public Organizador() {
        this.ativo = true;
    }

    public Organizador(String nome, LocalDate dataNascimento, String sexo, String email,
                       String senha, String cnpj, String razaoSocial, String nomeFantasia) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.email = email;
        this.senha = senha;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.ativo = true;
    }

    public String getIdadeCompleta() {
        if (dataNascimento == null) return "";

        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(dataNascimento, hoje);

        return String.format("%d anos, %d meses e %d dias",
                periodo.getYears(), periodo.getMonths(), periodo.getDays());
    }

    public boolean isEmpresa() {
        return cnpj != null && !cnpj.isEmpty();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organizador that = (Organizador) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Organizador{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                ", cnpj='" + cnpj + '\'' +
                '}';
    }
}