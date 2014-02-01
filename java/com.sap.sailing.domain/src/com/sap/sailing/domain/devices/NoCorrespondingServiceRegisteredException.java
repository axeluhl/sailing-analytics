package com.sap.sailing.domain.devices;

public class NoCorrespondingServiceRegisteredException extends RuntimeException {
    private final String type;
    private static final long serialVersionUID = -358955216089477585L;

    public NoCorrespondingServiceRegisteredException(String message, String type) {
        super(message);
        this.type = type;
    }

    public String getDeviceType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + "(Type: " + type + ")";
    }
}
