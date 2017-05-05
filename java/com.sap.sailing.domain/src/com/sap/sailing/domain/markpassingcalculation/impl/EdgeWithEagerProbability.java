package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.markpassingcalculation.Candidate;

/**
 * An edge that has a fixed {@link #getProbability() probability} provided to the constructor.
 * 
 * @author Nicolas Klose
 * 
 */
public class EdgeWithEagerProbability extends Edge {
    private final double estimatedDistanceAndStartTimingProbability;

    public EdgeWithEagerProbability(Candidate start, Candidate end, double estimatedDistanceAndStartTimingProbability, int numberOfWaypoints) {
        super(start, end, numberOfWaypoints);
        this.estimatedDistanceAndStartTimingProbability = estimatedDistanceAndStartTimingProbability;
    }
    
    @Override
    protected double getEstimatedDistanceAndStartTimingProbability() {
        return estimatedDistanceAndStartTimingProbability;
    }
}