package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;

/**
 * A speed, convertable in various units of measure. Can be negative.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Speed extends Comparable<Speed> {
    final static Speed NULL = new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0));
    
    double getKnots();

    double getMetersPerSecond();

    double getKilometersPerHour();
    
    double getBeaufort();

    /**
     * Traveling at this speed starting at time <code>from</code> until time </code>to</code>, how far have we traveled?
     * If <code>to</code> is before </code>from</code>, the speed will be applied in reverse. If this speed has a negative
     * amount then so will the resulting distance.
     */
    Distance travel(TimePoint from, TimePoint to);

}
