package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.enums.StatusIngresso;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.exceptions.IngressoJaCanceladoException;
import br.com.softhouse.dende.model.Ingresso;
import br.com.softhouse.dende.repositories.util.CrudRepository;
import br.com.softhouse.dende.repositories.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class IngressoRepository implements CrudRepository<Ingresso, Long> {

    private final ConnectionPool connectionPool;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;

    public IngressoRepository(ConnectionPool connectionPool,
                              UsuarioRepository usuarioRepository,
                              EventoRepository eventoRepository) {
        this.connectionPool = connectionPool;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
    }

    private Ingresso mapRow(ResultSet rs) throws SQLException {
        Ingresso ingresso = new Ingresso();
        ingresso.setId(rs.getLong("id"));
        ingresso.setValorPago(rs.getDouble("valor_pago"));

        double valorEstornado = rs.getDouble("valor_estornado");
        ingresso.setValorReembolsado(rs.wasNull() ? null : valorEstornado);

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            ingresso.setStatus(StatusIngresso.valueOf(statusStr));
        }

        Timestamp dataCompra = rs.getTimestamp("data_compra");
        if (dataCompra != null) ingresso.setDataCompra(dataCompra.toLocalDateTime());

        Timestamp dataCancelamento = rs.getTimestamp("data_cancelamento");
        if (dataCancelamento != null) ingresso.setDataCancelamento(dataCancelamento.toLocalDateTime());

        long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            usuarioRepository.findById(usuarioId).ifPresent(ingresso::setUsuario);
        }

        long eventoId = rs.getLong("evento_id");
        if (!rs.wasNull()) {
            eventoRepository.findById(eventoId).ifPresent(ingresso::setEvento);
        }

        return ingresso;
    }

    @Override
    public Ingresso save(Ingresso ingresso) {
        if (ingresso.getId() == null) {
            return insert(ingresso);
        }
        return update(ingresso);
    }

    @Override
    public Optional<Ingresso> findById(Long id) {
        String sql = "SELECT * FROM ingresso WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao buscar ingresso por id: " + id, e);
        }
    }

    @Override
    public List<Ingresso> findAll() {
        String sql = "SELECT * FROM ingresso ORDER BY data_compra DESC";
        return executarListagem(sql);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM ingresso WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar ingresso com id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM ingresso WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao verificar existência de ingresso.", e);
        }
    }

    @Override
    public List<Ingresso> findAllAtivos() {
        String sql = "SELECT * FROM ingresso WHERE status = 'ATIVO' ORDER BY data_compra DESC";
        return executarListagem(sql);
    }

    public List<Ingresso> findByUsuarioId(Long usuarioId) {
        String sql = """
                SELECT i.*
                FROM ingresso i
                INNER JOIN evento e ON i.evento_id = e.id
                WHERE i.usuario_id = ?
                ORDER BY
                    CASE
                        WHEN i.status = 'ATIVO' AND e.ativo = 1 AND e.data_fim > NOW()
                        THEN 0 ELSE 1
                    END,
                    e.data_inicio,
                    e.nome
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, usuarioId);
            return executarListagemComResultSet(ps);

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao listar ingressos do usuário: " + usuarioId, e);
        }
    }

    public List<Ingresso> findByEventoId(Long eventoId) {
        String sql = "SELECT * FROM ingresso WHERE evento_id = ? ORDER BY data_compra";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventoId);
            return executarListagemComResultSet(ps);

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao listar ingressos do evento: " + eventoId, e);
        }
    }

    public List<Ingresso> findAtivosDoEvento(Long eventoId) {
        String sql = "SELECT * FROM ingresso WHERE evento_id = ? AND status = 'ATIVO'";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventoId);
            return executarListagemComResultSet(ps);

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao listar ingressos ativos do evento: " + eventoId, e);
        }
    }

    public int contarIngressosAtivos(Long eventoId) {
        String sql = "SELECT COUNT(*) FROM ingresso WHERE evento_id = ? AND status = 'ATIVO'";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao contar ingressos ativos do evento: " + eventoId, e);
        }
    }

    public Ingresso cancelar(Long ingressoId, double taxaCancelamento) {
        Ingresso ingresso = findById(ingressoId)
                .orElseThrow(() -> new DatabaseOperationException(
                        "Ingresso não encontrado com id: " + ingressoId));

        if (ingresso.getStatus() == StatusIngresso.CANCELADO) {
            throw new IngressoJaCanceladoException(ingressoId);
        }

        ingresso.cancelar(taxaCancelamento);
        return update(ingresso);
    }

    private Ingresso insert(Ingresso ingresso) {
        String sql = """
                INSERT INTO ingresso
                    (usuario_id, evento_id, valor_pago, status, valor_estornado, data_compra, data_cancelamento)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, ingresso);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ingresso.setId(keys.getLong(1));
                }
            }
            return ingresso;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir ingresso.", e);
        }
    }

    private Ingresso update(Ingresso ingresso) {
        String sql = """
                UPDATE ingresso
                SET usuario_id = ?, evento_id = ?, valor_pago = ?,
                    status = ?, valor_estornado = ?,
                    data_compra = ?, data_cancelamento = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, ingresso);
            ps.setLong(8, ingresso.getId());
            ps.executeUpdate();
            return ingresso;

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao atualizar ingresso com id: " + ingresso.getId(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Ingresso i) throws SQLException {
        if (i.getUsuario() != null && i.getUsuario().getId() != null) {
            ps.setLong(1, i.getUsuario().getId());
        } else {
            throw new DatabaseOperationException("Usuário é obrigatório para o ingresso.");
        }

        if (i.getEvento() != null && i.getEvento().getId() != null) {
            ps.setLong(2, i.getEvento().getId());
        } else {
            throw new DatabaseOperationException("Evento é obrigatório para o ingresso.");
        }

        ps.setDouble(3, i.getValorPago() != null ? i.getValorPago() : 0.0);
        ps.setString(4, i.getStatus() != null ? i.getStatus().name() : "ATIVO");

        if (i.getValorReembolsado() != null) {
            ps.setDouble(5, i.getValorReembolsado());
        } else {
            ps.setDouble(5, 0.0);
        }

        ps.setTimestamp(6, i.getDataCompra() != null
                ? Timestamp.valueOf(i.getDataCompra()) : new Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(7, i.getDataCancelamento() != null
                ? Timestamp.valueOf(i.getDataCancelamento()) : null);
    }

    private List<Ingresso> executarListagem(String sql) {
        List<Ingresso> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar ingressos.", e);
        }
    }

    private List<Ingresso> executarListagemComResultSet(PreparedStatement ps) throws SQLException {
        List<Ingresso> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }
}
