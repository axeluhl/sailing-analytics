package com.sap.sailing.domain.common.racelog.tracking;

public class NoCorrespondingServiceRegisteredException extends RuntimeException {
    public String type;
    public Class<?> serviceType;
    private static final long serialVersionUID = -358955216089477585L;
    
    protected NoCorrespondingServiceRegisteredException() {}

    public NoCorrespondingServiceRegisteredException(String message, String type, Class<?> serviceType) {
        super(message);
        this.type = type;
        this.serviceType = serviceType;
    }

    public String getDeviceType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + " (Service: " + serviceType.getSimpleName() + ", Type: " + type + ")";
    }
}
