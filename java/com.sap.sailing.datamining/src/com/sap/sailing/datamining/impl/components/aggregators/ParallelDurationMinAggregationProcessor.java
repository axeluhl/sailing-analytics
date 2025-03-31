package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMinAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDurationMinAggregationProcessor extends
        AbstractParallelComparableMinAggregationProcessor<Duration> {
    
    private static final AggregationProcessorDefinition<Duration, Duration> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, Duration.class, "Minimum", ParallelDurationMinAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, Duration> getDefinition() {
        return DEFINITION;
    }

    public ParallelDurationMinAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Duration>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }
}
