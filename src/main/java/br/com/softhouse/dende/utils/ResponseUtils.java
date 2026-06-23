package br.com.softhouse.dende.utils;

import br.com.dende.softhouse.process.route.ResponseEntity;


public class ResponseUtils {

    // Para respostas de sucesso com String
    public static ResponseEntity<String> ok(String message) {
        return ResponseEntity.ok(message);
    }

    // Para respostas de sucesso com Object (como Map, List, etc)
    public static ResponseEntity<Object> ok(Object data) {
        return ResponseEntity.ok(data);
    }

    // Para erros com mensagem (retorna String)
    public static ResponseEntity<String> badRequest(String message) {
        // Como não temos um método específico, retornamos a mensagem de erro
        // mas ainda usando ok (o framework pode tratar o status depois)
        return ResponseEntity.ok("ERRO: " + message);
    }

    // Para not found com mensagem (retorna String)
    public static ResponseEntity<String> notFound(String message) {
        return ResponseEntity.ok("NÃO ENCONTRADO: " + message);
    }
}