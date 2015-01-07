package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.SpeedWithBearing;

public interface GPSFix extends Positioned, Timed, WithValidityCache, WithEstimatedSpeedCache {
    /**
     * Reaches <code>to</code> at <code>to</code>'s {@link GPSFix#getTimePoint() time point} starting at this fix,
     * traveling on a straight great circle segment.
     * 
     * @return the speed over ground and course over ground required to reach <code>to</code> starting at this fix,
     *         traveling along a great circle segment
     */
    SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to);
}