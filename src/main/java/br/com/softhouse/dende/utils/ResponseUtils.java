package br.com.softhouse.dende.utils;

import br.com.dende.softhouse.process.route.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

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

    public static ResponseEntity<Object> badRequest(Object data) {
        return ResponseEntity.ok(data);
    }

    public static ResponseEntity<String> notFound(String message) {
        return ResponseEntity.ok("NÃO ENCONTRADO: " + message);
    }

    public static ResponseEntity<Object> notFound(Object data) {
        return ResponseEntity.ok(data);
    }

    public static Map<String, String> createErrorMap(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", message);
        return error;
    }
}