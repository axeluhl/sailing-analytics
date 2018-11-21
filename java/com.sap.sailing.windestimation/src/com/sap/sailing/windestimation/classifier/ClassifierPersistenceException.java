package com.sap.sailing.windestimation.classifier;

public class ClassifierPersistenceException extends Exception {

    private static final long serialVersionUID = -7672415389872133921L;

    public ClassifierPersistenceException() {
    }

    public ClassifierPersistenceException(String message) {
        super(message);
    }

    public ClassifierPersistenceException(Throwable cause) {
        super(cause);
    }

    public ClassifierPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassifierPersistenceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
