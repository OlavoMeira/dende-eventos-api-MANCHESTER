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
public class EmpresaRepository implements CrudRepository<Empresa, String> {

    private final ConnectionPool connectionPool;

    public EmpresaRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setCnpj(rs.getString("cnpj"));
        e.setRazaoSocial(rs.getString("razao_social"));
        e.setNomeFantasia(rs.getString("nome_fantasia"));

        Date dataAbertura = rs.getDate("data_abertura");
        if (dataAbertura != null) {
            e.setDataAbertura(dataAbertura.toLocalDate());
        }
        return e;
    }

    @Override
    public Empresa save(Empresa empresa) {
        if (existsByCnpj(empresa.getCnpj())) {
            return update(empresa);
        }
        return insert(empresa);
    }

    @Override
    public Optional<Empresa> findById(String cnpj) {
        String sql = "SELECT * FROM empresa WHERE cnpj = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cnpj);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao buscar empresa por CNPJ: " + cnpj, e);
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
    public void deleteById(String cnpj) {
        String sql = "DELETE FROM empresa WHERE cnpj = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cnpj);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar empresa com CNPJ: " + cnpj, e);
        }
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public List<Empresa> findAllAtivos() {
        return List.of();
    }

    public boolean existsByCnpj(String cnpj) {
        String sql = "SELECT COUNT(*) FROM empresa WHERE cnpj = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cnpj);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao verificar CNPJ de empresa.", e);
        }
    }

    private Empresa insert(Empresa empresa) {
        String sql = """
                INSERT INTO empresa (cnpj, razao_social, nome_fantasia, data_abertura)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, empresa);
            ps.executeUpdate();
            return empresa;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir empresa.", e);
        }
    }

    private Empresa update(Empresa empresa) {
        String sql = """
                UPDATE empresa
                SET razao_social = ?, nome_fantasia = ?, data_abertura = ?
                WHERE cnpj = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empresa.getRazaoSocial());
            ps.setString(2, empresa.getNomeFantasia());
            if (empresa.getDataAbertura() != null) {
                ps.setDate(3, Date.valueOf(empresa.getDataAbertura()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, empresa.getCnpj());
            ps.executeUpdate();
            return empresa;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao atualizar empresa com CNPJ: " + empresa.getCnpj(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Empresa empresa) throws SQLException {
        ps.setString(1, empresa.getCnpj());
        ps.setString(2, empresa.getRazaoSocial());
        ps.setString(3, empresa.getNomeFantasia());
        if (empresa.getDataAbertura() != null) {
            ps.setDate(4, Date.valueOf(empresa.getDataAbertura()));
        } else {
            ps.setNull(4, Types.DATE);
        }
    }
}
