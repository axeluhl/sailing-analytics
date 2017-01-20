package com.sap.sailing.grib.impl;

import com.sap.sailing.grib.GribWindField;
import com.sap.sailing.grib.GribWindFieldFactory;

import ucar.nc2.ft.FeatureDataset;

public class GribWindFieldFactoryImpl implements GribWindFieldFactory {
    @Override
    public GribWindField createGribWindField(FeatureDataset dataSet) {
        final GribWindField result;
        if (UVWindField.handles(dataSet)) {
            result = new UVWindField(dataSet);
        } else if (SpeedAndDirectionWindField.handles(dataSet)) {
            result = new SpeedAndDirectionWindField(dataSet);
        } else {
            throw new IllegalArgumentException("Couldn't find a wind field implementation handling data set "+dataSet);
        }
        return result;
    }
}
