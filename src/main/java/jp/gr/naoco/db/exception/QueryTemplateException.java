package jp.gr.naoco.db.exception;

public class QueryTemplateException extends RuntimeException {
    private static final long serialVersionUID = -4433316528682762230L;

    // /////////////////////////////////////////////////////////////////////////////////////////////

    // Constructor

    public QueryTemplateException(String message) {
        super(message);
    }

    public QueryTemplateException(String message, Throwable t) {
        super(message, t);
    }
}
