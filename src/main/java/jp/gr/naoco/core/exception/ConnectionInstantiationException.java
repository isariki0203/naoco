package jp.gr.naoco.core.exception;

public class ConnectionInstantiationException extends Exception {
    private static final long serialVersionUID = 4682449327101543261L;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ConnectionInstantiationException(String message, Throwable t) {
        super(message, t);
    }
}
