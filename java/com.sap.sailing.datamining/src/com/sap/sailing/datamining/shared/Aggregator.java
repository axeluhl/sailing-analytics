package com.sap.sailing.datamining.shared;

import java.util.Set;

import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface Aggregator {
    
    public double aggregate(Set<GPSFixMoving> fixes);

}
