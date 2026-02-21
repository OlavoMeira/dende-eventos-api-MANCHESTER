package br.com.softhouse.dende.model;

import br.com.softhouse.dende.enums.ModalidadeEvento;
import br.com.softhouse.dende.enums.TipoEvento;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Evento {
    private Long id;
    private String nome;
    private String paginaWeb; // Alinhado: Use "paginaWeb" no Postman
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim; // Alinhado: Use "dataFim" no Postman
    private TipoEvento tipoEvento;
    private ModalidadeEvento modalidade;
    private String local;
    private Integer capacidadeMaxima; // Alinhado: Use "capacidadeMaxima" no Postman
    private Double precoUnitarioIngresso;
    private Double taxaCancelamento;
    private boolean ativo;

    private Organizador organizador;
    private Evento eventoPrincipal;
    private List<Evento> subEventos = new ArrayList<>();
    private List<Ingresso> ingressos = new ArrayList<>();

    // Construtor corrigido com valores padrão para evitar erros de cálculo (NullPointer)
    public Evento() {
        this.ativo = true;
        this.capacidadeMaxima = 0;
        this.precoUnitarioIngresso = 0.0;
        this.taxaCancelamento = 0.0;
    }

    public boolean isLotado() {
        // Proteção: se capacidade for 0 ou nula, considera lotado para evitar erros
        if (capacidadeMaxima == null || capacidadeMaxima <= 0) return true;

        long ingressosAtivos = ingressos.stream()
                .filter(i -> i.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.ATIVO)
                .count();
        return ingressosAtivos >= capacidadeMaxima;
    }

    public boolean isFinalizado() {
        if (dataFim == null) return false;
        return dataFim.isBefore(LocalDateTime.now());
    }

    public boolean isEmAndamento() {
        if (dataInicio == null || dataFim == null) return false;
        LocalDateTime agora = LocalDateTime.now();
        return agora.isAfter(dataInicio) && agora.isBefore(dataFim);
    }

    public boolean isValidoParaCadastro() {
        if (dataInicio == null || dataFim == null) return false;
        
        LocalDateTime agora = LocalDateTime.now();

        // Regra: Não pode criar evento no passado
        if (dataInicio.isBefore(agora)) {
            return false;
        }

        // Regra: Fim deve ser após o início
        if (dataFim.isBefore(dataInicio)) {
            return false;
        }

        // Regra: Duração mínima de 30 minutos
        Duration duracao = Duration.between(dataInicio, dataFim);
        return duracao.toMinutes() >= 30;
    }

    public void adicionarIngresso(Ingresso ingresso) {
        if (ingressos == null) {
            ingressos = new ArrayList<>();
        }
        ingressos.add(ingresso);
    }

    public int getIngressosVendidos() {
        if (ingressos == null) return 0;
        return (int) ingressos.stream()
                .filter(i -> i.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.ATIVO)
                .count();
    }

    public int getVagasDisponiveis() {
        if (capacidadeMaxima == null) return 0;
        return capacidadeMaxima - getIngressosVendidos();
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = (capacidadeMaxima != null) ? capacidadeMaxima : 0; }

    public Double getPrecoUnitarioIngresso() { return precoUnitarioIngresso; }
    public void setPrecoUnitarioIngresso(Double precoUnitarioIngresso) { this.precoUnitarioIngresso = (precoUnitarioIngresso != null) ? precoUnitarioIngresso : 0.0; }

    public Double getTaxaCancelamento() { return taxaCancelamento; }
    public void setTaxaCancelamento(Double taxaCancelamento) { this.taxaCancelamento = (taxaCancelamento != null) ? taxaCancelamento : 0.0; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Organizador getOrganizador() { return organizador; }
    public void setOrganizador(Organizador organizador) { this.organizador = organizador; }

    public Evento getEventoPrincipal() { return eventoPrincipal; }
    public void setEventoPrincipal(Evento eventoPrincipal) { this.eventoPrincipal = eventoPrincipal; }

    public List<Evento> getSubEventos() { return subEventos; }
    public void setSubEventos(List<Evento> subEventos) { this.subEventos = subEventos; }

    public List<Ingresso> getIngressos() { return ingressos; }
    public void setIngressos(List<Ingresso> ingressos) { this.ingressos = ingressos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataInicio=" + dataInicio +
                ", ativo=" + ativo +
                '}';
    }
}