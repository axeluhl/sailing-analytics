package com.sap.sailing.datamining.shared;

import java.io.Serializable;

public interface ManeuverSpeedDetailsAggregation extends Serializable {
    
    double[] getValuePerAngle();
    
    int[] getCountPerAngle();
    
    int getCount();

}
