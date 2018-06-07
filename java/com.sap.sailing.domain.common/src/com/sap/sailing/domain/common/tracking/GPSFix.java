package com.sap.sailing.domain.common.tracking;

import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Timed;

public interface GPSFix extends Positioned, Timed, WithValidityCache, WithEstimatedSpeedCache {
    /**
     * A typical horizontal dilution of precision, or in other words, an error that is quite likely and may occur.
     * For example, when trying to tell whether an object was on the one or the other side of some other object,
     * this typical level of accuracy can be used as a default margin of error.
     */
    Distance TYPICAL_HDOP = new MeterDistance(5);
    
    /**
     * Reaches <code>to</code> at <code>to</code>'s {@link GPSFix#getTimePoint() time point} starting at this fix,
     * traveling on a straight great circle segment.
     * 
     * @return the speed over ground and course over ground required to reach <code>to</code> starting at this fix,
     *         traveling along a great circle segment
     */
    SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to);
}