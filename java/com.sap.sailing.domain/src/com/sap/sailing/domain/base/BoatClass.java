package com.sap.sailing.domain.base;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface BoatClass extends Named {
    /**
     * The distance returned by this method should be appropriate for use in
     * {@link TrackedRace#approximate(Competitor, Distance, TimePoint, TimePoint)} so that penalty circles and other
     * maneuvers are detected reliably.
     */
    Distance getMaximumDistanceForCourseApproximation();
    
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
