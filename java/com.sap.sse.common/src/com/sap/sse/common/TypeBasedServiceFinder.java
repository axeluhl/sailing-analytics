package com.sap.sse.common;


/**
 * @author Fredrik Teschke
 *
 * @param <ServiceType> Specifies desired the service interface
 */
public interface TypeBasedServiceFinder<ServiceType> {
    public static final String TYPE = "type";

    /**
     * Find a service that implements the <ServiceType> interface for the specified {@code type}
     * of object (e.g. type of a {@link GPSFix}).
     * @param type A {@link String} that specifies for which type the service should be registered
     * @throws NoCorrespondingServiceRegisteredException Thrown, if no service is registered for this type.
     */
    ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException;
    
    /**
     * Sets a fallback service (optional), that is used if no service is registered for a {@code type}.
     * This should be a generic service, that can deal with any {@code type}, and provides a
     * "better-than-nothing" implementation, which will be returned instead of throwing a
     * {@link NoCorrespondingServiceRegisteredException}.
     * <p>
     * This method does not need to be implemented, but can remain a stub, if no fallback behaviour
     * is desired.
     */
    void setFallbackService(ServiceType fallback);
}
