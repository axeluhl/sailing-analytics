package com.sap.sailing.datamining.impl.gpsfix;

import java.util.Collection;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.impl.GroupByDimension;

public class GroupGPSFixesByDimension<ValueType> extends GroupByDimension<GPSFixWithContext, ValueType> {

    public GroupGPSFixesByDimension(Collection<Dimension<GPSFixWithContext, ValueType>> dimensions) {
        super(dimensions);
    }
    
}
