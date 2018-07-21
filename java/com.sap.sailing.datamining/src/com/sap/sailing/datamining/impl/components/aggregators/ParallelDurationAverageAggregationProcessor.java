package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;

public class ParallelDurationAverageAggregationProcessor
            extends AbstractParallelAverageAggregationProcessor<Duration> {
    
    private static final Class<?> _c = AverageWithStats.class;
    @SuppressWarnings("unchecked")
    private static final Class<AverageWithStats<Duration>> _cc = (Class<AverageWithStats<Duration>>) _c;

    private static final AggregationProcessorDefinition<Duration, AverageWithStats<Duration>> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, _cc, "Average", ParallelDurationAverageAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, AverageWithStats<Duration>> getDefinition() {
        return DEFINITION;
    }

    public ParallelDurationAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, AverageWithStats<Duration>>, ?>> resultReceivers) {
        super(executor, resultReceivers, new ParallelDurationSumAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelDurationMinAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelDurationMaxAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()));
    }

    @Override
    public AggregationProcessorDefinition<Duration, AverageWithStats<Duration>> getProcessorDefinition() {
        return DEFINITION;
    }

    @Override
    protected Duration divide(Duration sum, long elementCount) {
        return sum.divide((double) elementCount);
    }

}
