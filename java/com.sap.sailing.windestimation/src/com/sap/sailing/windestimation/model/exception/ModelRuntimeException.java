package com.sap.sailing.windestimation.model.exception;

/**
 * Indicates that something wrong has happened during run-time where a machine learning model was involved.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ModelRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 170716968865186247L;

    public ModelRuntimeException() {
    }

    public ModelRuntimeException(String message) {
        super(message);
    }

    public ModelRuntimeException(Throwable cause) {
        super(cause);
    }

    public ModelRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
