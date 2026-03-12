package br.com.softhouse.dende.model.builder;

import br.com.softhouse.dende.enums.ModalidadeEvento;
import br.com.softhouse.dende.enums.TipoEvento;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;

import java.time.LocalDateTime;

public class EventoBuilder {

    private Long id;
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
    private boolean ativo;
    private Organizador organizador;
    private Evento eventoPrincipal;

    private EventoBuilder() {}

    public static EventoBuilder builder() {
        return new EventoBuilder();
    }

    public EventoBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public EventoBuilder nome(String nome) {
        this.nome = nome;
        return this;
    }

    public EventoBuilder paginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
        return this;
    }

    public EventoBuilder descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public EventoBuilder dataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
        return this;
    }

    public EventoBuilder dataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
        return this;
    }

    public EventoBuilder tipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
        return this;
    }

    public EventoBuilder modalidade(ModalidadeEvento modalidade) {
        this.modalidade = modalidade;
        return this;
    }

    public EventoBuilder local(String local) {
        this.local = local;
        return this;
    }

    public EventoBuilder capacidadeMaxima(Integer capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima != null ? capacidadeMaxima : 0;
        return this;
    }

    public EventoBuilder precoUnitarioIngresso(Double precoUnitarioIngresso) {
        this.precoUnitarioIngresso = precoUnitarioIngresso != null ? precoUnitarioIngresso : 0.0;
        return this;
    }

    public EventoBuilder taxaCancelamento(Double taxaCancelamento) {
        this.taxaCancelamento = taxaCancelamento != null ? taxaCancelamento : 0.0;
        return this;
    }

    public EventoBuilder ativo(boolean ativo) {
        this.ativo = ativo;
        return this;
    }

    public EventoBuilder organizador(Organizador organizador) {
        this.organizador = organizador;
        return this;
    }

    public EventoBuilder eventoPrincipal(Evento eventoPrincipal) {
        this.eventoPrincipal = eventoPrincipal;
        return this;
    }

    public Evento build() {
        Evento evento = new Evento();
        evento.setId(this.id);
        evento.setNome(this.nome);
        evento.setPaginaWeb(this.paginaWeb);
        evento.setDescricao(this.descricao);
        evento.setDataInicio(this.dataInicio);
        evento.setDataFim(this.dataFim);
        evento.setTipoEvento(this.tipoEvento);
        evento.setModalidade(this.modalidade);
        evento.setLocal(this.local);
        evento.setCapacidadeMaxima(this.capacidadeMaxima);
        evento.setPrecoUnitarioIngresso(this.precoUnitarioIngresso);
        evento.setTaxaCancelamento(this.taxaCancelamento);
        evento.setAtivo(this.ativo);
        evento.setOrganizador(this.organizador);
        evento.setEventoPrincipal(this.eventoPrincipal);
        return evento;
    }
}