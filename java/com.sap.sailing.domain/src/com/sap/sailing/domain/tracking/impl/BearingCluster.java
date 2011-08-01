package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;

/**
 * Contains a number of {@link Bearing} objects and maintains the average bearing. For a given {@link Bearing} it
 * can determine the difference to this cluster's average bearing.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BearingCluster {
    private final List<Bearing> bearings;
    private double sumDegrees;
    
    public BearingCluster() {
        bearings = new ArrayList<Bearing>();
        sumDegrees = 0.0;
    }
    
    public boolean isEmpty() {
        return bearings.isEmpty();
    }
    
    public void add(Bearing bearing) {
        bearings.add(bearing);
        sumDegrees += bearing.getDegrees();
    }
    
    public Bearing getAverage() {
        return new DegreeBearingImpl(sumDegrees / bearings.size());
    }
    
    /**
     * If there is no bearing stored in this cluster yet, 0.0 is returned.
     */
    public double getDifferenceFromAverage(Bearing bearing) {
        return bearings.size() == 0 ? 0.0 : Math.abs(sumDegrees / bearings.size() - bearing.getDegrees());
    }
}
