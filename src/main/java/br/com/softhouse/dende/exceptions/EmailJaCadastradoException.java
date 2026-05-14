package br.com.softhouse.dende.exceptions;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email, String tipo) {
        super(tipo + " com e-mail " + email + " já está cadastrado.");
    }

    public EmailJaCadastradoException(String message) {
        super(message);
    }
}