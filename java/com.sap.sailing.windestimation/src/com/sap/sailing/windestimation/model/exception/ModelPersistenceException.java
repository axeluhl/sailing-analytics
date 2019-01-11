package com.sap.sailing.windestimation.model.exception;

public class ModelPersistenceException extends Exception {

    private static final long serialVersionUID = -7672415389872133921L;

    public ModelPersistenceException() {
    }

    public ModelPersistenceException(String message) {
        super(message);
    }

    public ModelPersistenceException(Throwable cause) {
        super(cause);
    }

    public ModelPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelPersistenceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
