package com.dende.eventos.dto;

import com.dende.eventos.model.StatusEvento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class EventoDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "O nome do evento é obrigatório")
        @Size(min = 3, max = 100)
        private String nome;

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 500)
        private String descricao;

        @NotNull(message = "A data de início é obrigatória")
        private LocalDateTime dataInicio;

        @NotNull(message = "A data de fim é obrigatória")
        private LocalDateTime dataFim;

        @NotBlank(message = "O local é obrigatório")
        private String local;

        @NotNull(message = "A capacidade máxima é obrigatória")
        @Min(value = 1, message = "A capacidade mínima é 1")
        private Integer capacidadeMaxima;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String nome;
        private String descricao;
        private LocalDateTime dataInicio;
        private LocalDateTime dataFim;
        private String local;
        private Integer capacidadeMaxima;
        private int totalParticipantes;
        private StatusEvento status;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }
}
