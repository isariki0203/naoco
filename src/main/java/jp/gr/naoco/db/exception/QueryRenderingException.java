package jp.gr.naoco.db.exception;

public class QueryRenderingException extends RuntimeException {
    private static final long serialVersionUID = 7014540659781907158L;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public QueryRenderingException(String message) {
        super(message);
    }

    public QueryRenderingException(String message, Throwable t) {
        super(message, t);
    }
}
