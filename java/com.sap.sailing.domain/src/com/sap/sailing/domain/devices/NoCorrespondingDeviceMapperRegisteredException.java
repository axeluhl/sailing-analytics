package com.sap.sailing.domain.devices;

public class NoCorrespondingDeviceMapperRegisteredException extends RuntimeException {
    private final String deviceType;
    private static final long serialVersionUID = -358955216089477585L;

    public NoCorrespondingDeviceMapperRegisteredException(String message, String deviceType) {
        super(message);
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String toString() {
        return super.toString() + "(Device Type: " + deviceType + ")";
    }
}
