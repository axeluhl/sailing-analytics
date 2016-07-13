package com.sap.sailing.domain.common.tracking;

/**
 * Specific {@link SensorFix} for bravo devices (http://www.bravosystems.es/).
 */
public interface BravoFix extends SensorFix {

    /**
     * Gets the ride height (foiling) for this fix.
     * 
     * @return the ride height
     */
    double getRideHeight();
}
