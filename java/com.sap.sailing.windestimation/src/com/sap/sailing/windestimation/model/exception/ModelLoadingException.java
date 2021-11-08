package com.sap.sailing.windestimation.model.exception;

/**
 * Indicates that a machine learning model could not be loaded during runtime due to some unexpected error.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ModelLoadingException extends ModelRuntimeException {

    private static final long serialVersionUID = 9129472285700533042L;

    public ModelLoadingException() {
    }

    public ModelLoadingException(String message) {
        super(message);
    }

    public ModelLoadingException(Throwable cause) {
        super(cause);
    }

    public ModelLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelLoadingException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
