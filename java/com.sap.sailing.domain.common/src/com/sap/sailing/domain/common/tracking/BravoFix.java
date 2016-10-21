package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Distance;

/**
 * Specific {@link SensorFix} for bravo devices (http://www.bravosystems.es/).
 * 
 * <p><b>NOTE:</b> This fix type is currently used to track ESS foiling data, only.</p>
 */
public interface BravoFix extends SensorFix {

    /**
     * Gets the ride height (foiling) for this fix.
     * 
     * @return the ride height
     */
    Distance getRideHeight();
}
