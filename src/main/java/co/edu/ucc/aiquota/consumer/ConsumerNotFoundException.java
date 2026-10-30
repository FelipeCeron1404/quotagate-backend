package co.edu.ucc.aiquota.consumer;

public class ConsumerNotFoundException extends RuntimeException {

    public ConsumerNotFoundException(String consumerId) {
        super("No existe un usuario con el identificador '" + consumerId + "'");
    }
}
