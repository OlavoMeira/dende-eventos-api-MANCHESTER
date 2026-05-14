package br.com.softhouse.dende.repositories;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.exceptions.DatabaseOperationException;
import br.com.softhouse.dende.exceptions.EmailJaCadastradoException;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.util.CrudRepository;
import br.com.softhouse.dende.repositories.util.ConnectionPool;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UsuarioRepository implements CrudRepository<Usuario, Long> {

    private final ConnectionPool connectionPool;

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNome(rs.getString("nome"));
        u.setSexo(rs.getString("sexo"));
        u.setEmail(rs.getString("email"));
        u.setSenha(rs.getString("senha"));
        u.setAtivo(rs.getBoolean("ativo"));

        Date dataNasc = rs.getDate("data_nascimento");
        if (dataNasc != null) {
            u.setDataNascimento(dataNasc.toLocalDate());
        }
        return u;
    }

    public UsuarioRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            return insert(usuario);
        }
        return update(usuario);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
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
            throw new DatabaseOperationException("Erro ao buscar usuário por id: " + id, e);
        }
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT * FROM usuario ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao listar usuários.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao deletar usuário com id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<Usuario> findAllAtivos() {
        return List.of();
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
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
            throw new DatabaseOperationException("Erro ao buscar usuário por e-mail.", e);
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
            throw new DatabaseOperationException("Erro ao verificar e-mail de usuário.", e);
        }
    }

    private Usuario insert(Usuario usuario) {
        validarEmailUnico(usuario.getEmail(), null);

        String sql = """
                INSERT INTO usuario (nome, data_nascimento, sexo, email, senha, ativo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, usuario);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getLong(1));
                }
            }
            return usuario;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao inserir usuário.", e);
        }
    }

    private Usuario update(Usuario usuario) {
        validarEmailUnico(usuario.getEmail(), usuario.getId());

        String sql = """
                UPDATE usuario
                SET nome = ?, data_nascimento = ?, sexo = ?, email = ?, senha = ?, ativo = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, usuario);
            ps.setLong(7, usuario.getId());
            ps.executeUpdate();
            return usuario;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao atualizar usuário com id: " + usuario.getId(), e);
        }
    }

    private void preencherStatement(PreparedStatement ps, Usuario u) throws SQLException {
        ps.setString(1, u.getNome());
        if (u.getDataNascimento() != null) {
            ps.setDate(2, Date.valueOf(u.getDataNascimento()));
        } else {
            ps.setNull(2, Types.DATE);
        }
        ps.setString(3, u.getSexo());
        ps.setString(4, u.getEmail());
        ps.setString(5, u.getSenha());
        ps.setBoolean(6, u.isAtivo());
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
                    throw new EmailJaCadastradoException(email, "usuário");
                }
            }

        } catch (EmailJaCadastradoException e) {
            throw e;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Erro ao validar e-mail único de usuário.", e);
        }
    }
}
