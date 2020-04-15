package com.sap.sailing.domain.common.tagging;

public class TagAlreadyExistsException extends Exception {

    private static final long serialVersionUID = -3908869597780348971L;

    public TagAlreadyExistsException() {

    }

    public TagAlreadyExistsException(String message) {
        super(message);
    }
}
