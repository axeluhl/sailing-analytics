package com.sap.sailing.grib.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;

import ucar.nc2.ft.FeatureDataset;

public class SpeedAndDirectionWindField extends AbstractGribWindFieldImpl {

    public SpeedAndDirectionWindField(FeatureDataset dataSet) {
        super(dataSet);
    }

    @Override
    public Wind getWind(Position position) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Checks whether the data set has a wind speed variable (GRIB parameter #32) and a wind direction variable
     * (GRIB parameter #31).
     */
    public static boolean handles(FeatureDataset dataSet) {
        return hasVariable(dataSet, 31) && hasVariable(dataSet, 32);
    }

}
