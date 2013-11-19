package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.SingleThreadedDataRetriever;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetriever;
import com.sap.sailing.datamining.impl.trackedLegOfCompetitor.TrackedLegOfCompetitorRetriever;
import com.sap.sailing.datamining.shared.DataTypes;

public class DataRetrieverWorkerBuilder<DataType> implements WorkerBuilder<SingleThreadedDataRetriever<DataType>> {

    private final DataTypes dataType;

    public DataRetrieverWorkerBuilder(DataTypes dataType) {
        this.dataType = dataType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SingleThreadedDataRetriever<DataType> build() {
        switch (dataType) {
        case GPSFix:
            return (SingleThreadedDataRetriever<DataType>) new GPSFixRetriever();
        case TrackedLegOfCompetitor:
            return (SingleThreadedDataRetriever<DataType>) new TrackedLegOfCompetitorRetriever();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: " + dataType.toString());
    }

}
