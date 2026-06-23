package br.com.softhouse.dende.repositories;

import br.com.softhouse.dende.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Repositorio {

    private static Repositorio instance = new Repositorio();

    private final Map<Long, Usuario> usuarios;
    private final Map<Long, Organizador> organizadores;
    private final Map<Long, Evento> eventos;
    private final Map<Long, Ingresso> ingressos;

    private final AtomicLong usuarioIdCounter;
    private final AtomicLong organizadorIdCounter;
    private final AtomicLong eventoIdCounter;
    private final AtomicLong ingressoIdCounter;

    private Repositorio() {
        this.usuarios = new HashMap<>();
        this.organizadores = new HashMap<>();
        this.eventos = new HashMap<>();
        this.ingressos = new HashMap<>();

        this.usuarioIdCounter = new AtomicLong(1);
        this.organizadorIdCounter = new AtomicLong(1);
        this.eventoIdCounter = new AtomicLong(1);
        this.ingressoIdCounter = new AtomicLong(1);
    }

    public static Repositorio getInstance() {
        return instance;
    }

    // Usuário methods
    public Usuario salvarUsuario(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(usuarioIdCounter.getAndIncrement());
        }
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    public boolean existeUsuarioComEmail(String email) {
        return usuarios.values().stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }

    // Organizador methods
    public Organizador salvarOrganizador(Organizador organizador) {
        if (organizador.getId() == null) {
            organizador.setId(organizadorIdCounter.getAndIncrement());
        }
        organizadores.put(organizador.getId(), organizador);
        return organizador;
    }

    public Optional<Organizador> buscarOrganizadorPorId(Long id) {
        return Optional.ofNullable(organizadores.get(id));
    }

    public Optional<Organizador> buscarOrganizadorPorEmail(String email) {
        return organizadores.values().stream()
                .filter(o -> o.getEmail().equals(email))
                .findFirst();
    }

    public boolean existeOrganizadorComEmail(String email) {
        return organizadores.values().stream()
                .anyMatch(o -> o.getEmail().equals(email));
    }

    public boolean organizadorTemEventosAtivos(Long organizadorId) {
        return eventos.values().stream()
                .filter(e -> e.getOrganizador() != null && e.getOrganizador().getId().equals(organizadorId))
                .anyMatch(e -> e.isAtivo() && !e.isFinalizado());
    }

    // Evento methods
    public Evento salvarEvento(Evento evento) {
        if (evento.getId() == null) {
            evento.setId(eventoIdCounter.getAndIncrement());
        }
        eventos.put(evento.getId(), evento);
        return evento;
    }

    public Optional<Evento> buscarEventoPorId(Long id) {
        return Optional.ofNullable(eventos.get(id));
    }

    public List<Evento> listarEventosDoOrganizador(Long organizadorId) {
        return eventos.values().stream()
                .filter(e -> e.getOrganizador() != null && e.getOrganizador().getId().equals(organizadorId))
                .collect(Collectors.toList());
    }

    public List<Evento> listarEventosAtivos() {
        LocalDateTime agora = LocalDateTime.now();
        return eventos.values().stream()
                .filter(Evento::isAtivo)
                .filter(e -> e.getDataFim().isAfter(agora))
                .filter(e -> !e.isLotado())
                .sorted(Comparator.comparing(Evento::getDataInicio)
                        .thenComparing(Evento::getNome))
                .collect(Collectors.toList());
    }

    public void cancelarIngressosDoEvento(Long eventoId) {
        List<Ingresso> ingressosEvento = listarIngressosDoEvento(eventoId);
        Evento evento = eventos.get(eventoId);

        ingressosEvento.forEach(ingresso -> {
            ingresso.cancelar(evento.getTaxaCancelamento());
        });
    }

    // Ingresso methods
    public Ingresso salvarIngresso(Ingresso ingresso) {
        if (ingresso.getId() == null) {
            ingresso.setId(ingressoIdCounter.getAndIncrement());
        }
        ingressos.put(ingresso.getId(), ingresso);

        Evento evento = ingresso.getEvento();
        if (evento != null) {
            evento.adicionarIngresso(ingresso);
        }

        return ingresso;
    }

    public Optional<Ingresso> buscarIngressoPorId(Long id) {
        return Optional.ofNullable(ingressos.get(id));
    }

    public List<Ingresso> listarIngressosDoUsuario(Long usuarioId) {
        

        return ingressos.values().stream()
                .filter(i -> i.getUsuario() != null && i.getUsuario().getId().equals(usuarioId))
                .sorted((i1, i2) -> {
                    Evento e1 = i1.getEvento();
                    Evento e2 = i2.getEvento();

                    boolean e1Ativo = e1 != null && e1.isAtivo() && !e1.isFinalizado() &&
                            i1.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.ATIVO;
                    boolean e2Ativo = e2 != null && e2.isAtivo() && !e2.isFinalizado() &&
                            i2.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.ATIVO;

                    if (e1Ativo && !e2Ativo) return -1;
                    if (!e1Ativo && e2Ativo) return 1;

                    if (e1 != null && e2 != null) {
                        int dataCompare = e1.getDataInicio().compareTo(e2.getDataInicio());
                        if (dataCompare != 0) return dataCompare;
                        return e1.getNome().compareTo(e2.getNome());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    public List<Ingresso> listarIngressosDoEvento(Long eventoId) {
        return ingressos.values().stream()
                .filter(i -> i.getEvento() != null && i.getEvento().getId().equals(eventoId))
                .collect(Collectors.toList());
    }

    public List<Ingresso> listarIngressosAtivosDoEvento(Long eventoId) {
        return ingressos.values().stream()
                .filter(i -> i.getEvento() != null && i.getEvento().getId().equals(eventoId))
                .filter(i -> i.getStatus() == br.com.softhouse.dende.enums.StatusIngresso.ATIVO)
                .collect(Collectors.toList());
    }
}