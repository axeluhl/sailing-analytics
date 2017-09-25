package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

public interface ManeuverSpeedDetailsStatistic {

    double[] getManeuverValuePerTWA();

    NauticalSide getManeuverDirection();

    ManeuverSpeedDetailsSettings getSettings();

}