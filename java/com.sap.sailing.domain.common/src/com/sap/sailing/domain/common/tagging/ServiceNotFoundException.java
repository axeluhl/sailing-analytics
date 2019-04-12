package com.sap.sailing.domain.common.tagging;

public class ServiceNotFoundException extends Exception {

    private static final long serialVersionUID = 4354757061181985766L;

    public ServiceNotFoundException() {

    }

    public ServiceNotFoundException(String message) {
        super(message);
    }
}