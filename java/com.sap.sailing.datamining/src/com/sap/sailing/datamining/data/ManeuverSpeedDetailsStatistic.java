package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverSpeedDetailsStatistic {

    double[] getManeuverValuePerTWA();

    NauticalSide getManeuverDirection();

    ManeuverSpeedDetailsSettings getSettings();

}