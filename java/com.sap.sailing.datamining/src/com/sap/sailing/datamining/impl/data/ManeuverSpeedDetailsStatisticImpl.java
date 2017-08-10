package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;

public class ManeuverSpeedDetailsStatisticImpl implements ManeuverSpeedDetailsStatistic {
    
    private double[] maneuverValuePerTWA;

    public ManeuverSpeedDetailsStatisticImpl(double[] maneuverValuePerTWA) {
        this.maneuverValuePerTWA = maneuverValuePerTWA;
    }

    @Override
    public double[] getManeuverValuePerTWA() {
        return maneuverValuePerTWA;
    }

}