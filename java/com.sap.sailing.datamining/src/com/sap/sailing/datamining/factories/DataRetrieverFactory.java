package com.sap.sailing.datamining.factories;

import java.util.concurrent.Executor;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.impl.AbstractSingleThreadedDataRetriever;
import com.sap.sailing.datamining.impl.ParallelDataRetriever;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetriever;
import com.sap.sailing.datamining.impl.trackedLegOfCompetitor.TrackedLegOfCompetitorRetriever;
import com.sap.sailing.datamining.shared.DataTypes;

public final class DataRetrieverFactory {

    private DataRetrieverFactory() {
    }

    /**
     * Creates a retriever for the given data type. Throws an exception, if the used <code>DataType</code> doesn't match
     * the <code>DataType</code> of the returning retriever.
     */
    public static <DataType> DataRetriever<DataType> createDataRetriever(DataTypes dataType, Executor executor) {
        AbstractSingleThreadedDataRetriever<DataType> workerBase = createSingleThreadedDataRetriever(dataType);
        return new ParallelDataRetriever<DataType>(workerBase, executor);
    }

    @SuppressWarnings("unchecked")
    private static <DataType> AbstractSingleThreadedDataRetriever<DataType> createSingleThreadedDataRetriever(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (AbstractSingleThreadedDataRetriever<DataType>) new GPSFixRetriever();
        case TrackedLegOfCompetitor:
            return (AbstractSingleThreadedDataRetriever<DataType>) new TrackedLegOfCompetitorRetriever();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: " + dataType.toString());
    }

}
