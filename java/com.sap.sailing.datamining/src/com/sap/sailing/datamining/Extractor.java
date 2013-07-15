package com.sap.sailing.datamining;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface Extractor extends Serializable {
    
    public List<Double> extractDataFrom(List<GPSFixMoving> gpsFixes);

}
