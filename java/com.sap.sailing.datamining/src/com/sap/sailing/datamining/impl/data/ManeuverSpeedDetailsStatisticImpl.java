package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.domain.common.NauticalSide;

public class ManeuverSpeedDetailsStatisticImpl implements ManeuverSpeedDetailsStatistic {
    
    private double[] maneuverValuePerTWA;
    private NauticalSide maneuverDirection;
    private boolean maneuverDirectionEqualWeightingEnabled;

    public ManeuverSpeedDetailsStatisticImpl(double[] maneuverValuePerTWA, NauticalSide maneuverDirection, boolean maneuverDirectionEqualWeightingEnabled) {
        this.maneuverValuePerTWA = maneuverValuePerTWA;
        this.maneuverDirection = maneuverDirection;
        this.maneuverDirectionEqualWeightingEnabled = maneuverDirectionEqualWeightingEnabled;
    }

    @Override
    public double[] getManeuverValuePerTWA() {
        return maneuverValuePerTWA;
    }
    
    @Override
    public NauticalSide getManeuverDirection() {
        return maneuverDirection;
    }
    
    @Override
    public boolean isManeuverDirectionEqualWeightingEnabled() {
        return maneuverDirectionEqualWeightingEnabled;
    }

}