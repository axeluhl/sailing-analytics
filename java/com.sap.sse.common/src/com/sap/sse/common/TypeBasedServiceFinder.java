package com.sap.sse.common;

import java.util.Set;

/**
 * Manages a set of services implementing {@code ServiceType}. Each of these services provides similar functionality
 * (e.g. a storage service), but for a different {@code type} of input/output (e.g. storage for competitors or GPS
 * fixes).<p>
 * 
 * Methods exist for {@link #findService(String) finding a service for a certain type}, or {@link #findAllServices()
 * finding all services} that implement {@code ServiceType}.<p>
 * 
 * An implementation of this interface might e.g. be backed by the OSGi service registry. For that case, this
 * OSGi-agnostic interface allows such a {@code TypeBasedServiceFinder} object to be introduced in contexts that are not
 * OSGi-aware, and also allows for easier mocking/dummy implementations in unit testing.
 * 
 * @author Fredrik Teschke
 *
 * @param <ServiceType>
 *            Specifies desired the service interface
 */
public interface TypeBasedServiceFinder<ServiceType> {
    public static final String TYPE = "type";
    
    public interface Callback<ServiceType> {
        void withService(ServiceType service);
    }

    /**
     * Find a service that implements the <ServiceType> interface for the specified {@code type} of object (e.g. type of
     * a {@link GPSFix}).
     * 
     * @param type
     *            A {@link String} that specifies for which type the service should be registered
     * @throws NoCorrespondingServiceRegisteredException
     *             Thrown, if no service is registered for this type.
     */
    ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException;
    
    /**
     * Similar to {@link #findService(String)}; when the service for {@code type} is already available,
     * the {@code callback} is immediately {@link Callback#withService(Object) invoked} with the service
     * found. If the service is not found, instead of throwing a {@link NoCorrespondingServiceRegisteredException},
     * the {@code callback} is recorded by this finder; when at a later point in time a service with {@code type}
     * becomes available, all callbacks recorded this way will be {@link Callback#withService(Object) invoked}
     * with the service that became available. The method returns immediately after recording the callback in
     * this case.
     */
    void applyServiceWhenAvailable(String type, Callback<ServiceType> callback);

    Set<ServiceType> findAllServices();

    /**
     * Sets a fallback service (optional), that is used if no service is registered for a {@code type}. This should be a
     * generic service, that can deal with any {@code type}, and provides a "better-than-nothing" implementation, which
     * will be returned instead of throwing a {@link NoCorrespondingServiceRegisteredException}.
     * <p>
     * This method does not need to be implemented, but can remain a stub, if no fallback behaviour is desired.
     */
    void setFallbackService(ServiceType fallback);
}
