package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.shared.SharedDimension;

public interface DimensionManager<DataType> {

    public Dimension<DataType, ?> getDimensionFor(SharedDimension sharedDimension);

}
