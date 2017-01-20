package com.sap.sailing.grib.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;

import ucar.nc2.ft.FeatureDataset;

public class UVWindField extends AbstractGribWindFieldImpl {

    public UVWindField(FeatureDataset dataSet) {
        super(dataSet);
    }

    @Override
    public Wind getWind(Position position) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Checks whether the data set has a u-component of wind (GRIB parameter #33) and a v-component of wind
     * (GRIB parameter #34).
     */
    public static boolean handles(FeatureDataset dataSet) {
        return hasVariable(dataSet, 33) && hasVariable(dataSet, 34);
    }
}
