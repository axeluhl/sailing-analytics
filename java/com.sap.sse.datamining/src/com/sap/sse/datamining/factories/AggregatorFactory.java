package com.sap.sse.datamining.factories;

import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.deprecated.GroupDividingParallelAggregator;
import com.sap.sse.datamining.impl.workers.builders.AggregationWorkerBuilder;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.components.ElementType;
import com.sap.sse.datamining.workers.AggregationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class AggregatorFactory {
    
    private AggregatorFactory() { }

    public static <ExtractedType, AggregatedType> ParallelAggregator<ExtractedType, AggregatedType> createAggregator(DataMiningStringMessages stringMessages,
                                                                                                                    Locale locale,
                                                                                                                    ElementType valueType, AggregatorType aggregatorType, ThreadPoolExecutor executor) {
        WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> workerBuilder = new AggregationWorkerBuilder<ExtractedType, AggregatedType>(valueType, aggregatorType);
        return new GroupDividingParallelAggregator<ExtractedType, AggregatedType>(stringMessages.get(locale, aggregatorType.getNameMessage()), workerBuilder, executor);
    }

}
