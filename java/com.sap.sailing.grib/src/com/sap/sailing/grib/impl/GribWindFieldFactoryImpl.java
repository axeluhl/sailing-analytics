package com.sap.sailing.grib.impl;

import com.sap.sailing.grib.GribWindField;
import com.sap.sailing.grib.GribWindFieldFactory;

import ucar.nc2.ft.FeatureDataset;

public class GribWindFieldFactoryImpl implements GribWindFieldFactory {
    @Override
    public GribWindField createGribWindField(FeatureDataset... dataSets) {
        final GribWindField result;
        if (UVWindField.handles(dataSets)) {
            result = new UVWindField(dataSets);
        } else if (SpeedAndDirectionWindField.handles(dataSets)) {
            result = new SpeedAndDirectionWindField(dataSets);
        } else {
            throw new IllegalArgumentException("Couldn't find a wind field implementation handling data set(s) "+dataSets);
        }
        return result;
    }
}
