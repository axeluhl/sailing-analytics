package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public abstract class AbstractParallelGroupedDataAggregationProcessor<ExtractedType, AggregatedType>
                extends AbstractParallelAggregationProcessor<GroupedDataEntry<ExtractedType>, Map<GroupKey, AggregatedType>> {

    @SuppressWarnings("unchecked")
    public AbstractParallelGroupedDataAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers, String aggregationNameMessageKey) {
        super((Class<GroupedDataEntry<ExtractedType>>)(Class<?>) GroupedDataEntry.class, (Class<Map<GroupKey, AggregatedType>>)(Class<?>) Map.class,
                executor, resultReceivers, aggregationNameMessageKey);
    }

}
