package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.exceptions.EmailJaCadastradoException;
import br.com.softhouse.dende.model.Empresa;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.util.CrudRepository;
import br.com.softhouse.dende.repositories.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class OrganizadorRepository implements CrudRepository<Organizador, Long> {

    private final ConnectionPool connectionPool;
    private final EmpresaRepository empresaRepository;

    public OrganizadorRepository(ConnectionPool connectionPool,
                                 EmpresaRepository empresaRepository) {
        this.connectionPool = connectionPool;
        this.empresaRepository = empresaRepository;
    }

    private Organizador mapRow(ResultSet rs) throws SQLException {
        Organizador o = new Organizador();
        o.setId(rs.getLong("id"));
        o.setNome(rs.getString("nome"));
        o.setSexo(rs.getString("sexo"));
        o.setEmail(rs.getString("email"));
        o.setSenha(rs.getString("senha"));
        o.setAtivo(rs.getBoolean("ativo"));

        Date dataNasc = rs.getDate("data_nascimento");
        if (dataNasc != null) {
            o.setDataNascimento(dataNasc.toLocalDate());
        }

        String cnpj = rs.getString("empresa_cnpj");
        if (cnpj != null && !cnpj.isBlank()) {
            empresaRepository.findById(cnpj).ifPresent(o::setEmpresa);
        }
        return o;
    }

    @Override
    public Organizador save(Organizador organizador) {
        // Persiste a empresa primeiro (se existir)
        if (organizador.getEmpresa() != null) {
            empresaRepository.save(organizador.getEmpresa());
        }

        if (organizador.getId() == null) {
            return insert(organizador);
        }
        return update(organizador);
    }

    @Override
    public Optional<Organizador> findById(Long id) {
        String sql = "SELECT * FROM organizador WHERE id = ?";
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
            throw new DatabaseOperationException("Erro ao buscar organizador por id: " + id, e);
        }
    }

    @Override
    public List<Organizador> findAll() {
        String sql = "SELECT * FROM organizador ORDER BY nome";
        List<Organizador> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar organizadores.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM organizador WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar organizador com id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<Organizador> findAllAtivos() {
        return List.of();
    }

    public Optional<Organizador> findByEmail(String email) {
        String sql = "SELECT * FROM organizador WHERE email = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao buscar organizador por e-mail.", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM organizador WHERE email = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao verificar e-mail de organizador.", e);
        }
    }

    public boolean possuiEventosAtivos(Long organizadorId) {
        String sql = """
                SELECT COUNT(*) FROM evento
                WHERE organizador_id = ?
                  AND ativo = true
                  AND data_fim > NOW()
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, organizadorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao verificar eventos ativos do organizador: " + organizadorId, e);
        }
    }

    private Organizador insert(Organizador organizador) {
        validarEmailUnico(organizador.getEmail(), null);

        String sql = """
                INSERT INTO organizador
                    (nome, data_nascimento, sexo, email, senha, ativo, empresa_cnpj)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, organizador);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    organizador.setId(keys.getLong(1));
                }
            }
            return organizador;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir organizador.", e);
        }
    }

    private Organizador update(Organizador organizador) {
        validarEmailUnico(organizador.getEmail(), organizador.getId());

        String sql = """
                UPDATE organizador
                SET nome = ?, data_nascimento = ?, sexo = ?, email = ?,
                    senha = ?, ativo = ?, empresa_cnpj = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, organizador);
            ps.setLong(8, organizador.getId());
            ps.executeUpdate();
            return organizador;

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao atualizar organizador com id: " + organizador.getId(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Organizador o) throws SQLException {
        ps.setString(1, o.getNome());
        if (o.getDataNascimento() != null) {
            ps.setDate(2, Date.valueOf(o.getDataNascimento()));
        } else {
            ps.setNull(2, Types.DATE);
        }
        ps.setString(3, o.getSexo());
        ps.setString(4, o.getEmail());
        ps.setString(5, o.getSenha());
        ps.setBoolean(6, o.isAtivo());

        Empresa empresa = o.getEmpresa();
        if (empresa != null && empresa.getCnpj() != null) {
            ps.setString(7, empresa.getCnpj());
        } else {
            ps.setNull(7, Types.VARCHAR);
        }
    }

    private void validarEmailUnico(String email, Long ignorarId) {
        String sql = ignorarId == null
                ? "SELECT COUNT(*) FROM organizador WHERE email = ?"
                : "SELECT COUNT(*) FROM organizador WHERE email = ? AND id <> ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            if (ignorarId != null) {
                ps.setLong(2, ignorarId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new EmailJaCadastradoException(email, "organizador");
                }
            }

        } catch (EmailJaCadastradoException e) {
            throw e;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao validar e-mail único de organizador.", e);
        }
    }
}
