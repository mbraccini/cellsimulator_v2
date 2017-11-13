package exceptions;

public class AtmException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AtmException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AtmException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public AtmException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public AtmException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public AtmException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
}
