package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.enums.ModalidadeEvento;
import br.com.softhouse.dende.enums.TipoEvento;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.model.Evento;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.util.CrudRepository;
import br.com.softhouse.dende.repositories.util.ConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EventoRepository implements CrudRepository<Evento, Long> {

    private final ConnectionPool connectionPool;
    private final OrganizadorRepository organizadorRepository;

    public EventoRepository(ConnectionPool connectionPool,
                            OrganizadorRepository organizadorRepository) {
        this.connectionPool = connectionPool;
        this.organizadorRepository = organizadorRepository;
    }

    private Evento mapRow(ResultSet rs) throws SQLException {
        Evento.EventoBuilder builder = Evento.builder()
                .id(rs.getLong("id"))
                .nome(rs.getString("nome"))
                .paginaWeb(rs.getString("pagina_web"))
                .descricao(rs.getString("descricao"))
                .local(rs.getString("local"))
                .ativo(rs.getBoolean("ativo"));

        Timestamp dataInicio = rs.getTimestamp("data_inicio");
        if (dataInicio != null) builder.dataInicio(dataInicio.toLocalDateTime());

        Timestamp dataFim = rs.getTimestamp("data_fim");
        if (dataFim != null) builder.dataFim(dataFim.toLocalDateTime());

        String tipoStr = rs.getString("tipo_evento");
        if (tipoStr != null) builder.tipoEvento(TipoEvento.valueOf(tipoStr));

        String modalidadeStr = rs.getString("modalidade");
        if (modalidadeStr != null) builder.modalidade(ModalidadeEvento.valueOf(modalidadeStr));

        builder.capacidadeMaxima(rs.getInt("capacidade_maxima"));
        builder.precoUnitarioIngresso(rs.getDouble("preco_unitario_ingresso"));
        builder.taxaCancelamento(rs.getDouble("taxa_cancelamento"));

        long organizadorId = rs.getLong("organizador_id");
        if (!rs.wasNull()) {
            organizadorRepository.findById(organizadorId)
                    .ifPresent(builder::organizador);
        }

        return builder.build();
    }

    @Override
    public Evento save(Evento evento) {
        if (evento.getId() == null) {
            return insert(evento);
        }
        return update(evento);
    }

    @Override
    public Optional<Evento> findById(Long id) {
        String sql = "SELECT * FROM evento WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evento evento = mapRow(rs);
                    loadEventoPrincipal(evento, rs.getLong("evento_principal_id"),
                            rs.wasNull());
                    return Optional.of(evento);
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao buscar evento por id: " + id, e);
        }
    }

    @Override
    public List<Evento> findAll() {
        String sql = "SELECT * FROM evento ORDER BY data_inicio";
        return executarListagem(sql);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM evento WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar evento com id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<Evento> findAllAtivos() {
        return List.of();
    }

    public List<Evento> findByOrganizadorId(Long organizadorId) {
        String sql = "SELECT * FROM evento WHERE organizador_id = ? ORDER BY data_inicio";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, organizadorId);
            return executarListagemComResultSet(ps);

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao listar eventos do organizador: " + organizadorId, e);
        }
    }

    public List<Evento> findAtivosComVagas() {
        String sql = """
                SELECT e.*,
                       (e.capacidade_maxima - COUNT(i.id)) AS vagas
                FROM evento e
                LEFT JOIN ingresso i
                    ON i.evento_id = e.id AND i.status = 'ATIVO'
                WHERE e.ativo = true
                  AND e.data_fim > NOW()
                GROUP BY e.id
                HAVING vagas > 0
                ORDER BY e.data_inicio, e.nome
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return executarListagemComResultSet(ps);

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar feed de eventos.", e);
        }
    }

    public void cancelarIngressosDoEvento(Long eventoId, double taxaCancelamento) {
        String sql = """
                UPDATE ingresso
                SET status = 'CANCELADO',
                    data_cancelamento = NOW(),
                    valor_reembolsado = valor_pago * ?
                WHERE evento_id = ? AND status = 'ATIVO'
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, 1.0 - taxaCancelamento);
            ps.setLong(2, eventoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao cancelar ingressos do evento: " + eventoId, e);
        }
    }

    public boolean organizadorTemEventosAtivos(Long organizadorId) {
        return organizadorRepository.possuiEventosAtivos(organizadorId);
    }

    private Evento insert(Evento evento) {
        String sql = """
                INSERT INTO evento
                    (nome, pagina_web, descricao, data_inicio, data_fim,
                     tipo_evento, modalidade, local, capacidade_maxima,
                     preco_unitario_ingresso, taxa_cancelamento, ativo,
                     organizador_id, evento_principal_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, evento);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    evento.setId(keys.getLong(1));
                }
            }
            return evento;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir evento.", e);
        }
    }

    private Evento update(Evento evento) {
        String sql = """
                UPDATE evento
                SET nome = ?, pagina_web = ?, descricao = ?, data_inicio = ?, data_fim = ?,
                    tipo_evento = ?, modalidade = ?, local = ?, capacidade_maxima = ?,
                    preco_unitario_ingresso = ?, taxa_cancelamento = ?, ativo = ?,
                    organizador_id = ?, evento_principal_id = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, evento);
            ps.setLong(15, evento.getId());
            ps.executeUpdate();
            return evento;

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao atualizar evento com id: " + evento.getId(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Evento e) throws SQLException {
        ps.setString(1, e.getNome());
        ps.setString(2, e.getPaginaWeb());
        ps.setString(3, e.getDescricao());

        ps.setTimestamp(4, e.getDataInicio() != null
                ? Timestamp.valueOf(e.getDataInicio()) : null);
        ps.setTimestamp(5, e.getDataFim() != null
                ? Timestamp.valueOf(e.getDataFim()) : null);

        ps.setString(6, e.getTipoEvento() != null ? e.getTipoEvento().name() : null);
        ps.setString(7, e.getModalidade() != null ? e.getModalidade().name() : null);
        ps.setString(8, e.getLocal());
        ps.setInt(9, e.getCapacidadeMaxima() != null ? e.getCapacidadeMaxima() : 0);
        ps.setDouble(10, e.getPrecoUnitarioIngresso() != null ? e.getPrecoUnitarioIngresso() : 0.0);
        ps.setDouble(11, e.getTaxaCancelamento() != null ? e.getTaxaCancelamento() : 0.0);
        ps.setBoolean(12, e.isAtivo());

        if (e.getOrganizador() != null && e.getOrganizador().getId() != null) {
            ps.setLong(13, e.getOrganizador().getId());
        } else {
            ps.setNull(13, Types.BIGINT);
        }

        if (e.getEventoPrincipal() != null && e.getEventoPrincipal().getId() != null) {
            ps.setLong(14, e.getEventoPrincipal().getId());
        } else {
            ps.setNull(14, Types.BIGINT);
        }
    }

    private void loadEventoPrincipal(Evento evento, long principalId, boolean wasNull) {
        if (!wasNull && principalId > 0) {
            findById(principalId).ifPresent(evento::setEventoPrincipal);
        }
    }

    private List<Evento> executarListagem(String sql) {
        List<Evento> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Evento ev = mapRow(rs);
                loadEventoPrincipal(ev, rs.getLong("evento_principal_id"), rs.wasNull());
                lista.add(ev);
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar eventos.", e);
        }
    }

    private List<Evento> executarListagemComResultSet(PreparedStatement ps) throws SQLException {
        List<Evento> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento ev = mapRow(rs);
                loadEventoPrincipal(ev, rs.getLong("evento_principal_id"), rs.wasNull());
                lista.add(ev);
            }
        }
        return lista;
    }
}
