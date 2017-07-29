package jp.gr.naoco.core.exception;

public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -1704882648542930839L;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable t) {
        super(message, t);
    }
}
