package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.shared.DimensionIdentifier;

public interface DimensionManager<DataType> {

    public Dimension<DataType, ?> getDimensionFor(DimensionIdentifier sharedDimension);

}
