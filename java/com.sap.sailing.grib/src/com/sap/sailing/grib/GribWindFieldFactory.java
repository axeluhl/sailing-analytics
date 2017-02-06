package com.sap.sailing.grib;

import com.sap.sailing.grib.impl.GribWindFieldFactoryImpl;

import ucar.nc2.ft.FeatureDataset;

/**
 * Factory interface used to create objects of type {@link GribWindField} based on a {@link FeatureDataset}.
 * 
 * Example Usage:
 * <pre>
 *      final Formatter errorLog = new Formatter(System.err);
 *      final ucar.nc2.util.CancelTask task = null;
 *      final String location = "resources/globalMarineNetCroatia.grb.bz2"; // or could be some URI / URL from where to retrieve it
 *      final FeatureDataset dataSet = FeatureDatasetFactoryManager.open(FeatureType.ANY, location, task, errorLog);
 *      final GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindField(dataSet);
 * </pre>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface GribWindFieldFactory {
    GribWindFieldFactory INSTANCE = new GribWindFieldFactoryImpl();
    
    GribWindField createGribWindField(FeatureDataset... dataSet);
}
