package com.dende.eventos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erros.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Erro de validação");
        response.setErros(erros);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Erro interno: " + ex.getMessage()));
    }

    public static class ErrorResponse {
        private int status;
        private String mensagem;
        private LocalDateTime timestamp;
        private Map<String, String> erros;

        public ErrorResponse(int status, String mensagem) {
            this.status = status;
            this.mensagem = mensagem;
            this.timestamp = LocalDateTime.now();
        }

        public int getStatus() { return status; }
        public String getMensagem() { return mensagem; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, String> getErros() { return erros; }
        public void setErros(Map<String, String> erros) { this.erros = erros; }
    }
}
