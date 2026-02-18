package br.com.softhouse.dende.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class Usuario {
    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private String sexo;
    private String email;
    private String senha;
    private boolean ativo;

    public Usuario() {
        this.ativo = true;
    }

    public Usuario(String nome, LocalDate dataNascimento, String sexo, String email, String senha) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.email = email;
        this.senha = senha;
        this.ativo = true;
    }

    public String getIdadeCompleta() {
        if (dataNascimento == null) return "";

        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(dataNascimento, hoje);

        return String.format("%d anos, %d meses e %d dias",
                periodo.getYears(), periodo.getMonths(), periodo.getDays());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", sexo='" + sexo + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}