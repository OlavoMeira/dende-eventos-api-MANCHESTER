package br.com.softhouse.dende.dto.response;

public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String dataNascimento;
    private String idadeCompleta;
    private String sexo;
    private String email;
    private boolean ativo;

    public UsuarioResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getIdadeCompleta() { return idadeCompleta; }
    public void setIdadeCompleta(String idadeCompleta) { this.idadeCompleta = idadeCompleta; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
