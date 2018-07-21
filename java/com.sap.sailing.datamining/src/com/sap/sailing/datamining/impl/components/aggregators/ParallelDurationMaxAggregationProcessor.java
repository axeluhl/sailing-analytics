package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMaxAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDurationMaxAggregationProcessor extends
        AbstractParallelComparableMaxAggregationProcessor<Duration> {
    
    private static final AggregationProcessorDefinition<Duration, Duration> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, Duration.class, "Maximum", ParallelDurationMaxAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, Duration> getDefinition() {
        return DEFINITION;
    }

    public ParallelDurationMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Duration>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }
}
