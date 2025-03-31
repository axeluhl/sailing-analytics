package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Speed;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMinAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelSpeedMinAggregationProcessor extends
    AbstractParallelComparableMinAggregationProcessor<Speed> {
    
    private static final AggregationProcessorDefinition<Speed, Speed> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Speed.class, Speed.class, "Minimum", ParallelSpeedMinAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Speed, Speed> getDefinition() {
        return DEFINITION;
    }

    
    public ParallelSpeedMinAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Speed>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }
}
