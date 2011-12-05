package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Distance;

public class BoatClassImpl extends NamedImpl implements BoatClass {
    /**
     * If the averaged courses over ground differ by at least this degree angle, a maneuver will
     * be assumed. Note that this should be much less than the tack angle because averaging may
     * span across the actual maneuver.
     */
    private static final double MANEUVER_DEGREE_ANGLE_THRESHOLD = /* minimumDegreeDifference */ 30.;

    private static final double MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_UPWIND = 45.;
    
    private static final double MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_DOWNWIND = 15.;

    private static final Distance MAXIMUM_DISTANCE_FOR_COURSE_APPROXIMATION = new MeterDistance(3);
    
    private final long approximateManeuverDurationInMilliseconds;

    public BoatClassImpl(String name) {
        super(name);
        approximateManeuverDurationInMilliseconds = 5000; // as discussed with Dennis Gehrlein
    }

    @Override
    public long getApproximateManeuverDurationInMilliseconds() {
        return approximateManeuverDurationInMilliseconds;
    }

    @Override
    public double getManeuverDegreeAngleThreshold() {
        return MANEUVER_DEGREE_ANGLE_THRESHOLD;
    }
    
    @Override
    public double getMinimumAngleBetweenDifferentTacksUpwind() {
        return MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_UPWIND;
    }

    @Override
    public double getMinimumAngleBetweenDifferentTacksDownwind() {
        return MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_DOWNWIND;
    }

    @Override
    public Distance getMaximumDistanceForCourseApproximation() {
        return MAXIMUM_DISTANCE_FOR_COURSE_APPROXIMATION;
    }

}
