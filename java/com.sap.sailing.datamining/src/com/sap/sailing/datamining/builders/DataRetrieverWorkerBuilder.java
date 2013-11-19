package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.DataRetrievalWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetriever;
import com.sap.sailing.datamining.impl.trackedLegOfCompetitor.TrackedLegOfCompetitorRetriever;
import com.sap.sailing.datamining.shared.DataTypes;

public class DataRetrieverWorkerBuilder<DataType> implements WorkerBuilder<DataRetrievalWorker<DataType>> {

    private final DataTypes dataType;

    public DataRetrieverWorkerBuilder(DataTypes dataType) {
        this.dataType = dataType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataRetrievalWorker<DataType> build() {
        switch (dataType) {
        case GPSFix:
            return (DataRetrievalWorker<DataType>) new GPSFixRetriever();
        case TrackedLegOfCompetitor:
            return (DataRetrievalWorker<DataType>) new TrackedLegOfCompetitorRetriever();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: " + dataType.toString());
    }

}
