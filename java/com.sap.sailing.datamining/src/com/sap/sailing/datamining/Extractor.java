package com.sap.sailing.datamining;

import java.util.List;

import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface Extractor {
    
    public List<Double> extractDataFrom(List<GPSFixMoving> gpsFixes);

}
