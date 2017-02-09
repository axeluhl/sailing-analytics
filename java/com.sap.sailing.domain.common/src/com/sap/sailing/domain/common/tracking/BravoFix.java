package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Specific {@link SensorFix} for bravo devices (http://www.bravosystems.es/).
 * 
 * <p><b>NOTE:</b> This fix type is currently used to track ESS foiling data, only.</p>
 */
public interface BravoFix extends SensorFix {
    static final Distance MIN_FOILING_HEIGHT_THRESHOLD = new MeterDistance(0.2);

    /**
     * Gets the ride height (foiling) for this fix.
     * 
     * @return the ride height
     */
    @Statistic(messageKey="rideHeight", resultDecimals=1)
    Distance getRideHeight();

    @Statistic(messageKey="rideHeightPort", resultDecimals=1)
    Distance getRideHeightPortHull();

    @Statistic(messageKey="rideHeightStarboard", resultDecimals=1)
    Distance getRideHeightStarboardHull();
    
    @Dimension(messageKey="IsFoiling")
    boolean isFoiling();

    @Statistic(messageKey="pitch", resultDecimals=1)
    double getPitch();

    @Statistic(messageKey="yaw", resultDecimals=1)
    double getYaw();

    @Statistic(messageKey="roll", resultDecimals=1)
    double getRoll();
}
