package com.sap.sailing.grib.impl;

import com.sap.sailing.grib.GribWindField;

import ucar.nc2.Attribute;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;

/**
 * Wraps a {@link GridDataset GRIB data set} that either has the "Wind direction" parameter #31 and the
 * "Wind speed" parameter #32, or the "u-component of wind" parameter #33 and the "v-component of wind"
 * parameter #34. If the GRIB source has wind for more than one vertical layer/level (z-axis), only the
 * first one is used.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractGribWindFieldImpl implements GribWindField {
    private final FeatureDataset dataSet;
    
    public AbstractGribWindFieldImpl(FeatureDataset dataSet) {
        this.dataSet = dataSet;
    }
    
    protected static boolean hasVariable(FeatureDataset dataSet, int variableId) {
        for (VariableSimpleIF variable : dataSet.getDataVariables()) {
            final Attribute idVariable = variable.findAttributeIgnoreCase("Grib1_Parameter");
            if (idVariable != null && idVariable.getNumericValue().intValue() == variableId) {
                return true;
            }
        }
        return false;
    }
    
    protected FeatureDataset getDataSet() {
        return dataSet;
    }
}
