package com.sap.sailing.domain.common.racelog.tracking;

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
     * @throws NoCorrespondingServiceRegisteredException
     */
    ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException;
}
