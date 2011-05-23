package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;

public interface Speed extends Comparable<Speed> {
    final static Speed NULL = new KnotSpeedImpl(0, new DegreeBearingImpl(0));
    
    double getKnots();

    double getMetersPerSecond();

    double getKilometersPerHour();

    /**
     * Traveling at this speed starting at time <code>from</code> until time </code>to</code>, how far have we traveled?
     * If <code>to</code> is before </code>from</code>, the speed will be applied in reverse.
     */
    Distance travel(TimePoint from, TimePoint to);

}
