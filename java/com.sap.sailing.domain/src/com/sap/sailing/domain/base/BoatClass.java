package com.sap.sailing.domain.base;

public interface BoatClass extends Named {
    long getApproximateManeuverDurationInMilliseconds();

    /**
     * If the averaged courses over ground differ by at least this degree angle, a maneuver will
     * be assumed. Note that this should be much less than the tack angle because averaging may
     * span across the actual maneuver.
     */
    double getManeuverDegreeAngleThreshold();

    double getMinimumAngleBetweenDifferentTacksDownwind();

    double getMinimumAngleBetweenDifferentTacksUpwind();
}
