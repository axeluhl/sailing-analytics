package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NauticalSide;

public interface ManeuverSpeedDetailsStatistic {
    
    double[] getManeuverValuePerTWA();
    
    NauticalSide getManeuverDirection();
    
    boolean isManeuverDirectionEqualWeightingEnabled();

}