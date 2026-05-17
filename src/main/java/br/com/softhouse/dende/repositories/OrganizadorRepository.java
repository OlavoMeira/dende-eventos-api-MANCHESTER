package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.exceptions.EmailJaCadastradoException;
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
        o.setTipoUsuario(rs.getString("tipo_usuario"));
        o.setAtivo(rs.getBoolean("ativo"));

        Date dataNasc = rs.getDate("data_nascimento");
        if (dataNasc != null) {
            o.setDataNascimento(dataNasc.toLocalDate());
        }

        empresaRepository.findByOrganizadorId(o.getId()).ifPresent(o::setEmpresa);
        
        return o;
    }

    @Override
    public Organizador save(Organizador organizador) {
        organizador.setTipoUsuario("ORGANIZADOR");
        if (organizador.getId() == null) {
            insert(organizador);
        } else {
            update(organizador);
        }

        if (organizador.getEmpresa() != null) {
            organizador.getEmpresa().setOrganizadorId(organizador.getId());
            empresaRepository.save(organizador.getEmpresa());
        }
        
        return organizador;
    }

    @Override
    public Optional<Organizador> findById(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ? AND tipo_usuario = 'ORGANIZADOR'";
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
        String sql = "SELECT * FROM usuario WHERE tipo_usuario = 'ORGANIZADOR' ORDER BY nome";
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
        String sql = "DELETE FROM usuario WHERE id = ? AND tipo_usuario = 'ORGANIZADOR'";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar organizador com id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE id = ? AND tipo_usuario = 'ORGANIZADOR'";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao verificar existência de organizador.", e);
        }
    }

    @Override
    public List<Organizador> findAllAtivos() {
        String sql = "SELECT * FROM usuario WHERE tipo_usuario = 'ORGANIZADOR' AND ativo = 1 ORDER BY nome";
        List<Organizador> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar organizadores ativos.", e);
        }
    }

    public Optional<Organizador> findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ? AND tipo_usuario = 'ORGANIZADOR'";
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
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
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

    private void insert(Organizador o) {
        validarEmailUnico(o.getEmail(), null);

        String sql = """
                INSERT INTO usuario
                    (nome, data_nascimento, sexo, email, senha, tipo_usuario, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, o);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    o.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir organizador.", e);
        }
    }

    private void update(Organizador o) {
        validarEmailUnico(o.getEmail(), o.getId());

        String sql = """
                UPDATE usuario
                SET nome = ?, data_nascimento = ?, sexo = ?, email = ?,
                    senha = ?, tipo_usuario = ?, ativo = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, o);
            ps.setLong(8, o.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Erro ao atualizar organizador com id: " + o.getId(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Organizador o) throws SQLException {
        ps.setString(1, o.getNome());
        if (o.getDataNascimento() != null) {
            ps.setDate(2, Date.valueOf(o.getDataNascimento()));
        } else {
            throw new DatabaseOperationException("Data de nascimento é obrigatória.");
        }
        ps.setString(3, o.getSexo());
        ps.setString(4, o.getEmail());
        ps.setString(5, o.getSenha());
        ps.setString(6, "ORGANIZADOR");
        ps.setInt(7, o.isAtivo() ? 1 : 0);
    }

    private void validarEmailUnico(String email, Long ignorarId) {
        String sql = ignorarId == null
                ? "SELECT COUNT(*) FROM usuario WHERE email = ?"
                : "SELECT COUNT(*) FROM usuario WHERE email = ? AND id <> ?";

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
