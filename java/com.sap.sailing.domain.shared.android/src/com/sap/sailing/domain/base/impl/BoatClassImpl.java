package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.BoatHullType;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class BoatClassImpl extends NamedImpl implements BoatClass {
    private static final long serialVersionUID = 7194912853476256420L;

    /**
     * If the averaged courses over ground differ by at least this degree angle, a maneuver will
     * be assumed. Note that this should be much less than the tack angle because averaging may
     * span across the actual maneuver.
     */
    private static final double MANEUVER_DEGREE_ANGLE_THRESHOLD = /* minimumDegreeDifference */ 30.;

    private static final double MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_UPWIND = 60.;
    
    private static final double MINIMUM_ANGLE_BETWEEN_DIFFERENT_TACKS_DOWNWIND = 25.;

    private static final Distance MAXIMUM_DISTANCE_FOR_COURSE_APPROXIMATION = new MeterDistance(3);

    /**
     * Upwind course-based wind estimations are pretty confident for most boat classes.
     */
    private static final double UPWIND_WIND_ESTIMATION_CONFIDENCE = .9;

    /**
     * Downwind estimations are less confident than upwind because jibing angles vary more.
     */
    private static final double DOWNWIND_WIND_ESTIMATION_CONFIDENCE = .5;
    
    private final long approximateManeuverDurationInMilliseconds;

    private final boolean typicallyStartsUpwind;
    
    private final String displayName;

    private final Distance hullLength;
    
    private final Distance hullBeam;
    
    private final BoatHullType hullType;

    public BoatClassImpl(String name, boolean typicallyStartsUpwind) {
        super(name);
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        approximateManeuverDurationInMilliseconds = 8000; // as discussed with Dennis Gehrlein
        // TODO see bug 911: these values need to come from a master data base
        hullLength = name.toLowerCase().contains("extreme") && name.contains("40")
                ? new MeterDistance(40*12*2.54/100)
                : new MeterDistance(5); // a good average for the olympic classes...
        this.displayName = null;
        this.hullBeam = null;
        this.hullType = null;
    }
    
    // return new BoatClassImpl(boatClassName, typicallyStartsUpwind, displayName, hullLengthInMeter, hullBeamInMeter, hullType);
    
    public BoatClassImpl(String name, boolean typicallyStartsUpwind, String displayName, 
            double hullLengthInMeter, double hullBeamInMeter, BoatHullType hullType) {
        super(name);
        this.typicallyStartsUpwind = typicallyStartsUpwind;     
        this.displayName = displayName;
        this.hullLength = new MeterDistance(hullLengthInMeter);
        this.hullBeam = new MeterDistance(hullBeamInMeter);
        this.hullType = hullType;
        approximateManeuverDurationInMilliseconds = 8000; // as discussed with Dennis Gehrlein
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

    @Override
    public boolean typicallyStartsUpwind() {
        return typicallyStartsUpwind;
    }

    @Override
    public Distance getHullLength() {
        return hullLength;
    }

    @Override
    public double getDownwindWindEstimationConfidence(int numberOfBoatsInSmallestCluster) {
        // Even for up to a million boats in the smallest cluster, the multiplier is still less than one.
        // The multiplier is 1/1000000 in case there is only one boat and approaches 1.0 for many boats.
        return DOWNWIND_WIND_ESTIMATION_CONFIDENCE * getConfidenceMultiplierForClusterSize(numberOfBoatsInSmallestCluster);
    }

    @Override
    public double getUpwindWindEstimationConfidence(int numberOfBoatsInSmallestCluster) {
        // Even for up to a million boats in the smallest cluster, the multiplier is still less than one.
        // The multiplier is 1/1000000 in case there is only one boat and approaches 1.0 for many boats.
        return UPWIND_WIND_ESTIMATION_CONFIDENCE * getConfidenceMultiplierForClusterSize(numberOfBoatsInSmallestCluster);
    }

    /**
     * Even for up to a million boats in the smallest cluster, the multiplier is still less than one.
     * The multiplier is 1/1000000 in case there is only one boat and approaches 1.0 for many boats.
     */
    private double getConfidenceMultiplierForClusterSize(int numberOfBoatsInSmallestCluster) {
        return 1.0 + 1.0/1000000.0 - 1.0/(double) numberOfBoatsInSmallestCluster;
    }

    @Override
    public BoatClass resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateBoatClass(getName(), typicallyStartsUpwind());
    }

    public String getDisplayName() {
        return displayName;
    }

    public Distance getHullBeam() {
        return hullBeam;
    }

    public BoatHullType getHullType() {
        return hullType;
    }

}
