package com.sap.sse.filestorage;

public class OperationFailedException extends Exception {
    private static final long serialVersionUID = 3361800673687196602L;
    
    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
