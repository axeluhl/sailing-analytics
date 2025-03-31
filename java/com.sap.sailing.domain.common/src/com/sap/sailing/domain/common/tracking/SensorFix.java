package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Timed;

/**
 * A fix having sensor data provided by a device. This kind of fix is intended to be used in a way so that all data can
 * be accessed in a generic way by using names for the specific values. The available value names are provided by the
 * associated SensorFixTrack.
 * 
 * {@link SensorFix} implementation typically don't directly have their data but wrap a
 * {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}.
 */
public interface SensorFix extends Timed {
    /**
     * Gets a value by its name.
     * The available value names are provided by the associated SensorFixTrack.
     * 
     * @param valueName the name of the value to get from this fix
     * @return the value
     * @throws IllegalArgumentException if the valueName isn't known by this fix
     */
    double get(String valueName) throws IllegalArgumentException;

    /**
     * Obtains the data array containing the raw values making this sensor fix.
     * The array returned is a copy, and writing to it will not affect this fix
     * in any way.
     */
    Double[] get();
}
