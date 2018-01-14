package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsStatisticImpl implements ManeuverSpeedDetailsStatistic {
    
    private double[] maneuverValuePerTWA;
    private NauticalSide maneuverDirection;
    private ManeuverSpeedDetailsSettings settings;

    public ManeuverSpeedDetailsStatisticImpl(double[] maneuverValuePerTWA, NauticalSide maneuverDirection, ManeuverSpeedDetailsSettings settings) {
        this.maneuverValuePerTWA = maneuverValuePerTWA;
        this.maneuverDirection = maneuverDirection;
        this.settings = settings;
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
    public ManeuverSpeedDetailsSettings getSettings() {
        return settings;
    }

}