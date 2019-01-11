package com.sap.sailing.windestimation.model.exception;

public class ModelOperationException extends ModelRuntimeException {

    private static final long serialVersionUID = 7308703386832127467L;

    public ModelOperationException() {
    }

    public ModelOperationException(String message) {
        super(message);
    }

    public ModelOperationException(Throwable cause) {
        super(cause);
    }

    public ModelOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelOperationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
