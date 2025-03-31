package com.sap.sse.common;

/**
 * A factory for creating {@link TypeBasedServiceFinder}s, for a specific type of service.
 * The desired service type is specified by the {@link Class} object passed to the
 * {@link #createServiceFinder(Class)} method.
 * 
 * The created {@link TypeBasedServiceFinder} can then be used to find the desired
 * service for a specific type. E.g., there may be a persistence service interface for
 * {@link GPSFix}es, and there are several different service registered, depending on the type
 * of {@link GPSFix}. The service finder then can find the appropriate service for each fix.
 * @author Fredrik Teschke
 *
 */
public interface TypeBasedServiceFinderFactory {
    <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz);
}
