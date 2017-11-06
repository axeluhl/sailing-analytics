package com.sap.sailing.domain.racelog.tracking;

/**
 * Some object that can supply a {@link SensorFixStore} can be registered with the OSGi
 * registry using this interface, so it can be discovered by others, and the sensor fix
 * store can then be obtained.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface SensorFixStoreSupplier {
    SensorFixStore getSensorFixStore();
}
