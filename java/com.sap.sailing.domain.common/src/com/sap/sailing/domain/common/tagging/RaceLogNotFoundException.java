package com.sap.sailing.domain.common.tagging;

public class RaceLogNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -1935079125197798550L;

    public RaceLogNotFoundException() {

    }

    public RaceLogNotFoundException(String message) {
        super(message);
    }
}
