package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDurationSumAggregationProcessor extends
        AbstractParallelSumAggregationProcessor<Duration> {
    
    private static final AggregationProcessorDefinition<Duration, Duration> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, Duration.class, "Sum", ParallelDurationSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, Duration> getDefinition() {
        return DEFINITION;
    }
    
    public ParallelDurationSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, Duration>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Duration add(Duration t1, Duration t2) {
        return t1.plus(t2);
    }
}
