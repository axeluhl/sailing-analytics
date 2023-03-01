package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Speed;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;

public class ParallelSpeedAverageAggregationProcessor
            extends AbstractParallelAverageAggregationProcessor<Speed> {
    private static final Class<?> _c = AverageWithStats.class;
    @SuppressWarnings("unchecked")
    private static final Class<AverageWithStats<Speed>> _cc = (Class<AverageWithStats<Speed>>) _c;

    private static final AggregationProcessorDefinition<Speed, AverageWithStats<Speed>> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Speed.class, _cc, "Average", ParallelSpeedAverageAggregationProcessor.class);
    
    public ParallelSpeedAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, AverageWithStats<Speed>>, ?>> resultReceivers) {
        super(executor, resultReceivers, new ParallelSpeedSumAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelSpeedMinAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelSpeedMaxAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()));
    }

    public static AggregationProcessorDefinition<Speed, AverageWithStats<Speed>> getDefinition() {
        return DEFINITION;
    }

    @Override
    public AggregationProcessorDefinition<Speed, AverageWithStats<Speed>> getProcessorDefinition() {
        return DEFINITION;
    }

    @Override
    protected Speed divide(Speed sum, long elementCount) {
        return sum.scale(1. / (double) elementCount);
    }
}
