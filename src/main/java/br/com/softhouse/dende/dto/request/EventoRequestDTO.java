package br.com.softhouse.dende.dto.request;

import br.com.softhouse.dende.enums.ModalidadeEvento;
import br.com.softhouse.dende.enums.TipoEvento;

import java.time.LocalDateTime;

public class EventoRequestDTO {

    private String nome;
    private String paginaWeb;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private TipoEvento tipoEvento;
    private ModalidadeEvento modalidade;
    private String local;
    private Integer capacidadeMaxima;
    private Double precoUnitarioIngresso;
    private Double taxaCancelamento;

    private Long eventoPrincipalId;

    public EventoRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPaginaWeb() { return paginaWeb; }
    public void setPaginaWeb(String paginaWeb) { this.paginaWeb = paginaWeb; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

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

    public Long getEventoPrincipalId() { return eventoPrincipalId; }
    public void setEventoPrincipalId(Long eventoPrincipalId) { this.eventoPrincipalId = eventoPrincipalId; }
}
