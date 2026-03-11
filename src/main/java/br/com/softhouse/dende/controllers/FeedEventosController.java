package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.GetMapping;
import br.com.dende.softhouse.annotations.request.RequestMapping;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.mapper.EventoMapper;
import br.com.softhouse.dende.repositories.Repositorio;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/eventos")
public class FeedEventosController {

    private final Repositorio repositorio;

    public FeedEventosController() {
        this.repositorio = Repositorio.getInstance();
    }

    @GetMapping
    public ResponseEntity<?> feedEventos() {

        List<EventoResponseDTO> eventos = repositorio.listarEventosAtivos()
                .stream()
                .map(EventoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventos);
    }
}
