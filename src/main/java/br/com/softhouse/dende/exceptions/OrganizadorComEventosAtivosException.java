package br.com.softhouse.dende.exceptions;

public class OrganizadorComEventosAtivosException extends RuntimeException {
    public OrganizadorComEventosAtivosException(Long organizadorId) {
        super("Não é possível desativar o organizador " + organizadorId
                + " pois ele possui eventos ativos ou em andamento.");
    }
}
