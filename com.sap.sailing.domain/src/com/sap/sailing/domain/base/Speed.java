package com.sap.sailing.domain.base;

public interface Speed {
    Bearing getBearing();

    double getKnots();

    double getMetersPerSecond();

    double getKilometersPerHour();

    /**
     * Traveling at this speed starting at time <code>from</code> until
     * time </code>to</code>, how far have we traveled? If <code>to</code>
     * is before </code>from</code>, the speed will be applied in reverse.
     */
    Distance travel(TimePoint from, TimePoint to);
}
