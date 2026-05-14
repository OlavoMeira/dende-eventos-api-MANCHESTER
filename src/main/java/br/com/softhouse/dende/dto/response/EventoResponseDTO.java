package br.com.softhouse.dende.dto.response;

import br.com.softhouse.dende.enums.ModalidadeEvento;
import br.com.softhouse.dende.enums.TipoEvento;

public class EventoResponseDTO {

    private Long id;
    private String nome;
    private String paginaWeb;
    private String descricao;
    private String dataInicio;
    private String dataFim;
    private TipoEvento tipoEvento;
    private ModalidadeEvento modalidade;
    private String local;
    private Integer capacidadeMaxima;
    private Double precoUnitarioIngresso;
    private Double taxaCancelamento;
    private boolean ativo;

    private int ingressosVendidos;
    private int vagasDisponiveis;

    private String organizador;

    private Long eventoPrincipalId;
    private String eventoPrincipalNome;

    public EventoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPaginaWeb() { return paginaWeb; }
    public void setPaginaWeb(String paginaWeb) { this.paginaWeb = paginaWeb; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }

    public ModalidadeEvento getModalidade() { return modalidade; }
    public void setModalidade(ModalidadeEvento modalidade) { this.modalidade = modalidade; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public Double getPrecoUnitarioIngresso() { return precoUnitarioIngresso; }
    public void setPrecoUnitarioIngresso(Double precoUnitarioIngresso) { this.precoUnitarioIngresso = precoUnitarioIngresso; }

    public Double getTaxaCancelamento() { return taxaCancelamento; }
    public void setTaxaCancelamento(Double taxaCancelamento) { this.taxaCancelamento = taxaCancelamento; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public int getIngressosVendidos() { return ingressosVendidos; }
    public void setIngressosVendidos(int ingressosVendidos) { this.ingressosVendidos = ingressosVendidos; }

    public int getVagasDisponiveis() { return vagasDisponiveis; }
    public void setVagasDisponiveis(int vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }

    public String getOrganizador() { return organizador; }
    public void setOrganizador(String organizador) { this.organizador = organizador; }

    public Long getEventoPrincipalId() { return eventoPrincipalId; }
    public void setEventoPrincipalId(Long eventoPrincipalId) { this.eventoPrincipalId = eventoPrincipalId; }

    public String getEventoPrincipalNome() { return eventoPrincipalNome; }
    public void setEventoPrincipalNome(String eventoPrincipalNome) { this.eventoPrincipalNome = eventoPrincipalNome; }
}
