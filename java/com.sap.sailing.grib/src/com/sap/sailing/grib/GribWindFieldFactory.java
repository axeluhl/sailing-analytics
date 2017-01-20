package com.sap.sailing.grib;

import com.sap.sailing.grib.impl.GribWindFieldFactoryImpl;

import ucar.nc2.ft.FeatureDataset;

public interface GribWindFieldFactory {
    GribWindFieldFactory INSTANCE = new GribWindFieldFactoryImpl();
    
    GribWindField createGribWindField(FeatureDataset dataSet);
}
