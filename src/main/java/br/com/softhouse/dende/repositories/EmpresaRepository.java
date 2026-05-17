package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.model.Empresa;
import br.com.softhouse.dende.repositories.util.CrudRepository;
import br.com.softhouse.dende.repositories.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EmpresaRepository implements CrudRepository<Empresa, Long> {

    private final ConnectionPool connectionPool;

    public EmpresaRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getLong("id"));
        e.setOrganizadorId(rs.getLong("organizador_id"));
        e.setCnpj(rs.getString("cnpj"));
        e.setRazaoSocial(rs.getString("razao_social"));
        e.setNomeFantasia(rs.getString("nome_fantasia"));
        return e;
    }

    @Override
    public Empresa save(Empresa empresa) {
        if (empresa.getId() == null) {
            return insert(empresa);
        }
        return update(empresa);
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        String sql = "SELECT * FROM empresa WHERE id = ?";
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
            throw new DatabaseOperationException("Erro ao buscar empresa por ID: " + id, e);
        }
    }

    public Optional<Empresa> findByOrganizadorId(Long organizadorId) {
        String sql = "SELECT * FROM empresa WHERE organizador_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, organizadorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao buscar empresa por organizador: " + organizadorId, e);
        }
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT * FROM empresa ORDER BY razao_social";
        List<Empresa> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar empresas.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM empresa WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar empresa com ID: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM empresa WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao verificar existência de empresa.", e);
        }
    }

    @Override
    public List<Empresa> findAllAtivos() {
        return findAll();
    }

    private Empresa insert(Empresa empresa) {
        String sql = """
                INSERT INTO empresa (organizador_id, cnpj, razao_social, nome_fantasia)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, empresa.getOrganizadorId());
            ps.setString(2, empresa.getCnpj());
            ps.setString(3, empresa.getRazaoSocial());
            ps.setString(4, empresa.getNomeFantasia());
            
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    empresa.setId(keys.getLong(1));
                }
            }
            return empresa;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir empresa.", e);
        }
    }

    private Empresa update(Empresa empresa) {
        String sql = """
                UPDATE empresa
                SET organizador_id = ?, cnpj = ?, razao_social = ?, nome_fantasia = ?
                WHERE id = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, empresa.getOrganizadorId());
            ps.setString(2, empresa.getCnpj());
            ps.setString(3, empresa.getRazaoSocial());
            ps.setString(4, empresa.getNomeFantasia());
            ps.setLong(5, empresa.getId());
            
            ps.executeUpdate();
            return empresa;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao atualizar empresa com ID: " + empresa.getId(), e);
        }
    }
}
