package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.GetMapping;
import br.com.dende.softhouse.annotations.request.RequestMapping;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.repositories.Repositorio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/eventos")
public class FeedEventosController {

    private final Repositorio repositorio;

    public FeedEventosController() {
        this.repositorio = Repositorio.getInstance();
    }

    @GetMapping
    public ResponseEntity<Object> feedEventos() {
        List<Map<String, Object>> eventos = repositorio.listarEventosAtivos()
                .stream()
                .map(evento -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", evento.getId());
                    map.put("nome", evento.getNome());
                    map.put("descricao", evento.getDescricao());
                    map.put("dataInicio", evento.getDataInicio().toString());
                    map.put("dataFim", evento.getDataFim().toString());
                    map.put("tipoEvento", evento.getTipoEvento());
                    map.put("modalidade", evento.getModalidade());
                    map.put("local", evento.getLocal());
                    map.put("precoIngresso", evento.getPrecoUnitarioIngresso());
                    map.put("capacidadeMaxima", evento.getCapacidadeMaxima());
                    map.put("ingressosVendidos", evento.getIngressosVendidos());
                    map.put("vagasDisponiveis", evento.getVagasDisponiveis());
                    map.put("organizador", evento.getOrganizador().getNome());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventos);
    }
}