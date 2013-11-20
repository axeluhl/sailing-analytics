package com.sap.sailing.datamining.factories;

import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.AggregationWorker;
import com.sap.sailing.datamining.ParallelAggregator;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.builders.AggregationWorkerBuilder;
import com.sap.sailing.datamining.impl.GroupDividingParallelAggregator;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.ValueType;

public final class AggregatorFactory {
    
    private AggregatorFactory() { }

    public static <ExtractedType, AggregatedType> ParallelAggregator<ExtractedType, AggregatedType> createAggregator(ValueType valueType,
                                                                                                                    AggregatorType aggregatorType,
                                                                                                                    ThreadPoolExecutor executor) {
        WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> workerBuilder = new AggregationWorkerBuilder<ExtractedType, AggregatedType>(valueType, aggregatorType);
        return new GroupDividingParallelAggregator<ExtractedType, AggregatedType>(aggregatorType.getName(), workerBuilder, executor);
    }

}
