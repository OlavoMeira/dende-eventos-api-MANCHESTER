package com.dende.eventos.controller;

import com.dende.eventos.dto.EventoDTO;
import com.dende.eventos.model.StatusEvento;
import com.dende.eventos.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gerenciamento de eventos")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @Operation(summary = "Listar todos os eventos",
               description = "Retorna a lista completa de eventos cadastrados. Pode filtrar por status.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EventoDTO.Response>> listarTodos(
            @RequestParam(required = false)
            @Parameter(description = "Filtrar por status: ATIVO, CANCELADO, ENCERRADO, ESGOTADO")
            StatusEvento status) {

        List<EventoDTO.Response> eventos = status != null
                ? eventoService.listarPorStatus(status)
                : eventoService.listarTodos();

        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/com-vagas")
    @Operation(summary = "Listar eventos com vagas disponíveis",
               description = "Retorna apenas os eventos ativos com vagas ainda disponíveis.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EventoDTO.Response>> listarComVagas() {
        return ResponseEntity.ok(eventoService.listarComVagas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                     content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EventoDTO.Response> buscarPorId(
            @PathVariable @Parameter(description = "ID do evento") Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo evento")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    })
    public ResponseEntity<EventoDTO.Response> criar(
            @Valid @RequestBody EventoDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.criar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    })
    public ResponseEntity<EventoDTO.Response> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoDTO.Request dto) {
        return ResponseEntity.ok(eventoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar evento",
               description = "Altera o status do evento para CANCELADO.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Evento cancelado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    })
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        eventoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento",
               description = "Remove permanentemente o evento. Não é permitido se houver participantes.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Evento excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
