package br.com.softhouse.dende.exceptions;

public class EventoLotadoException extends RuntimeException {
    public EventoLotadoException(Long eventoId) {
        super("O evento com id " + eventoId + " está lotado.");
    }

    public EventoLotadoException(String nomeEvento) {
        super("O evento '" + nomeEvento + "' está lotado.");
    }
}
