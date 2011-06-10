package com.sap.sailing.domain.base;

public interface SpeedWithBearing extends Speed {
    Bearing getBearing();

    /**
     * Traveling at this speed starting at time <code>from</code> in position <code>pos</code> until time
     * </code>to</code>, how far have we traveled? If <code>to</code> is before </code>from</code>, the speed will be
     * applied in reverse.
     */
    Position travelTo(Position pos, TimePoint from, TimePoint to);
}
