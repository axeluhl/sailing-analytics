package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sse.datamining.data.Dimension;

public interface DimensionManager<DataType> {

    public Dimension<DataType, ?> getDimensionFor(DimensionIdentifier sharedDimension);

}
