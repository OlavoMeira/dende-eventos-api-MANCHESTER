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

    // Ian fiz a retirada dos campos cnpj, razaoSocial, nomeFantasia  de dentro  da classe organizador.
    private Empresa empresa;

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
        this.ativo = true;

        //  Ian Fiz uma grantia para que o objeto só seja criado se o cnpj existir. 
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            this.empresa = new Empresa(cnpj, razaoSocial, nomeFantasia, null);
        }
    }

    public String getIdadeCompleta() {
        if (dataNascimento == null) return "";
        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(dataNascimento, hoje);
        return String.format("%d anos, %d meses e %d dias",
                periodo.getYears(), periodo.getMonths(), periodo.getDays());
    }

    public boolean isEmpresa() {
        return this.empresa != null && this.empresa.getCnpj() != null;
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

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }


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
                ", empresa=" + (isEmpresa() ? empresa.getCnpj() : "N/A") +
                '}';
    }
}