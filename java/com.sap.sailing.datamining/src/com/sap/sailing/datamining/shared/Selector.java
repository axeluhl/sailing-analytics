package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface Selector extends Serializable {
    
    public List<String> getXValues();
    
    public List<GPSFixMoving> getDataFor(String xValue);

}
