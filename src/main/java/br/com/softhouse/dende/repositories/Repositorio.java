package br.com.softhouse.dende.repositories;

import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.model.Usuario;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class Repositorio {

    private static Repositorio instance = new Repositorio();
    private final Map<Long, Usuario> usuariosComum;
    private final Map<Long, Organizador> organizadores;

    private Repositorio() {
        this.usuariosComum = new HashMap<>();
        this.organizadores = new HashMap<>();
    }

    public static Repositorio getInstance() {
        return instance;
    }

    // Métodos para Usuários
    public void salvarUsuario(Usuario usuario) {
        usuariosComum.put(usuario.getId(), usuario);
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuariosComum.get(id);
    }

    public Collection<Usuario> listarUsuarios() {
        return usuariosComum.values();
    }

    
    public void salvarOrganizador(Organizador organizador) {
        organizadores.put(organizador.getId(), organizador);
    }

    public Organizador buscarOrganizadorPorId(Long id) {
        return organizadores.get(id);
    }
}