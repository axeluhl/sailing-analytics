package com.sap.sailing.datamining.impl.workers.builders;

import com.sap.sailing.datamining.impl.tracked_leg_of_competitor.DistanceTraveledExtractionWorker;
import com.sap.sailing.datamining.impl.workers.SpeedInKnotsExtractionWorker;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sse.datamining.workers.ExtractionWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class ExtractionWorkerBuilder<DataType, ExtractedType> implements WorkerBuilder<ExtractionWorker<DataType, ExtractedType>> {

    private StatisticType statisticType;

    public ExtractionWorkerBuilder(StatisticType statisticType) {
        this.statisticType = statisticType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExtractionWorker<DataType, ExtractedType> build() {
        switch (statisticType) {
        case Speed:
            return (ExtractionWorker<DataType, ExtractedType>) new SpeedInKnotsExtractionWorker();
        case Distance:
            return (ExtractionWorker<DataType, ExtractedType>) new DistanceTraveledExtractionWorker();
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistic type: "
                + statisticType.toString());
    }

}
