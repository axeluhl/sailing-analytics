package com.sap.sailing.datamining.factories;

import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.builders.AggregationWorkerBuilder;
import com.sap.sailing.datamining.impl.GroupDividingParallelAggregator;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.ValueType;
import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.workers.AggregationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class AggregatorFactory {
    
    private AggregatorFactory() { }

    public static <ExtractedType, AggregatedType> ParallelAggregator<ExtractedType, AggregatedType> createAggregator(DataMiningStringMessages stringMessages,
                                                                                                                    Locale locale,
                                                                                                                    ValueType valueType, AggregatorType aggregatorType, ThreadPoolExecutor executor) {
        WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> workerBuilder = new AggregationWorkerBuilder<ExtractedType, AggregatedType>(valueType, aggregatorType);
        return new GroupDividingParallelAggregator<ExtractedType, AggregatedType>(stringMessages.get(locale, aggregatorType.getNameMessage()), workerBuilder, executor);
    }

}
