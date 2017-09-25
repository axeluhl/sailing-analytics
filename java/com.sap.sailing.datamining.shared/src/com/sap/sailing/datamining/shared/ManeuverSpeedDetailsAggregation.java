package com.sap.sailing.datamining.shared;

import java.io.Serializable;

public interface ManeuverSpeedDetailsAggregation extends Serializable {

    double[] getValuePerTWA();

    int[] getCountPerTWA();

    int getCount();

}
