package com.dende.eventos.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class ParticipanteDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "O nome do participante é obrigatório")
        @Size(min = 2, max = 100)
        private String nome;

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
        private String cpf;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String nome;
        private String email;
        private String cpf;
        private Long eventoId;
        private String eventoNome;
        private LocalDateTime dataInscricao;
    }
}
