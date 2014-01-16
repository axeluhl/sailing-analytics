package com.sap.sailing.datamining.dimensions;

import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.shared.DataTypes;

public final class DimensionManagerProvider {
    
    private DimensionManagerProvider() { }
    
    private static final DimensionManager<GPSFixWithContext> GPSFixDimensionManager = new GPSFixDimensionManager();
    private static final DimensionManager<TrackedLegOfCompetitorWithContext> TrackedLegOfCompetitorDimensionManager = new TrackedLegOfCompetitorDimensionManager();

    @SuppressWarnings("unchecked")
    public static <DataType> DimensionManager<DataType> getDimensionManagerFor(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (DimensionManager<DataType>) GPSFixDimensionManager;
        case TrackedLegOfCompetitor:
            return (DimensionManager<DataType>) TrackedLegOfCompetitorDimensionManager;
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

}
