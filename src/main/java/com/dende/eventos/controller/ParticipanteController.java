package com.dende.eventos.controller;

import com.dende.eventos.dto.ParticipanteDTO;
import com.dende.eventos.service.ParticipanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Participantes", description = "Gerenciamento de inscrições em eventos")
public class ParticipanteController {

    private final ParticipanteService participanteService;

    @GetMapping("/eventos/{eventoId}/participantes")
    @Operation(summary = "Listar participantes de um evento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<List<ParticipanteDTO.Response>> listarPorEvento(
            @PathVariable @Parameter(description = "ID do evento") Long eventoId) {
        return ResponseEntity.ok(participanteService.listarPorEvento(eventoId));
    }

    @GetMapping("/participantes/{id}")
    @Operation(summary = "Buscar participante por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante encontrado"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteDTO.Response> buscarPorId(
            @PathVariable @Parameter(description = "ID do participante") Long id) {
        return ResponseEntity.ok(participanteService.buscarPorId(id));
    }

    @PostMapping("/eventos/{eventoId}/participantes")
    @Operation(summary = "Inscrever participante em evento",
               description = "Realiza a inscrição de um participante em um evento ativo com vagas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Inscrição realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio (evento esgotado, duplicata, etc.)")
    })
    public ResponseEntity<ParticipanteDTO.Response> inscrever(
            @PathVariable Long eventoId,
            @Valid @RequestBody ParticipanteDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participanteService.inscrever(eventoId, dto));
    }

    @DeleteMapping("/participantes/{id}")
    @Operation(summary = "Cancelar inscrição",
               description = "Remove a inscrição de um participante de um evento.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inscrição cancelada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    })
    public ResponseEntity<Void> cancelarInscricao(@PathVariable Long id) {
        participanteService.cancelarInscricao(id);
        return ResponseEntity.noContent().build();
    }
}
