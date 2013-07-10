package com.sap.sailing.datamining.shared;

import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface Selector {
    
    public List<String> getXValues();
    
    public Set<GPSFixMoving> getDataFor(String xValue);

}
