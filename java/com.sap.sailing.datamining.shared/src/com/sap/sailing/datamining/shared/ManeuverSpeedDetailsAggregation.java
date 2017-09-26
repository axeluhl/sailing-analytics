package com.sap.sailing.datamining.shared;

import java.io.Serializable;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverSpeedDetailsAggregation extends Serializable {

    double[] getValuePerTWA();

    int[] getCountPerTWA();

    int getCount();

}
