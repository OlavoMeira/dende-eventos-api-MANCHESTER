package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.GetMapping;
import br.com.dende.softhouse.annotations.request.RequestMapping;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.response.EventoResponseDTO;
import br.com.softhouse.dende.services.EventoService;

import java.util.List;

@Controller
@RequestMapping(path = "/eventos")
public class FeedEventosController {

    private final EventoService eventoService;

    public FeedEventosController() {
        this.eventoService = new EventoService();
    }

    @GetMapping
    public ResponseEntity<Object> feedEventos() {
        List<EventoResponseDTO> response = eventoService.listarFeed();
        return ResponseEntity.ok(response);
    }
}