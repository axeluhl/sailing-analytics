package com.sap.sailing.datamining.factories;

import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.ExtractionWorker;
import com.sap.sailing.datamining.ParallelExtractor;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.builders.ExtractionWorkerBuilder;
import com.sap.sailing.datamining.impl.GroupDividingParallelExtractor;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public final class ExtractorFactory {
    
    private ExtractorFactory() { }
    
    public static <DataType, ExtractedType extends Number> ParallelExtractor<DataType, ExtractedType> createExtractor(StatisticType statisticType, ThreadPoolExecutor executor) {
        WorkerBuilder<ExtractionWorker<DataType, ExtractedType>> workerBuilder = new ExtractionWorkerBuilder<DataType, ExtractedType>(statisticType);
        return new GroupDividingParallelExtractor<DataType, ExtractedType>(statisticType.getSignifier(), statisticType.getUnit(), statisticType.getValueDecimals(),
                                                                           workerBuilder, executor);
    }

}
