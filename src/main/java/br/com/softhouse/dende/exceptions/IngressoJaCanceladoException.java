package br.com.softhouse.dende.exceptions;

public class IngressoJaCanceladoException extends RuntimeException {
    public IngressoJaCanceladoException(Long ingressoId) {
        super("O ingresso com id " + ingressoId + " já está cancelado.");
    }
}
