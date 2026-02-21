package br.com.softhouse.dende.utils;

import br.com.dende.softhouse.process.route.ResponseEntity;


public class ResponseUtils {

    public static ResponseEntity<String> ok(String message) {
        return ResponseEntity.ok(message);
    }

    public static ResponseEntity<Object> ok(Object data) {
        return ResponseEntity.ok(data);
    }

    public static ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.ok("ERRO: " + message);
    }

    public static ResponseEntity<String> notFound(String message) {
        return ResponseEntity.ok("NÃO ENCONTRADO: " + message);
    }
}