package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Distance;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;

public class ParallelDistanceAverageAggregationProcessor
            extends AbstractParallelAverageAggregationProcessor<Distance> {
    private static final Class<?> _c = AverageWithStats.class;
    @SuppressWarnings("unchecked")
    private static final Class<AverageWithStats<Distance>> _cc = (Class<AverageWithStats<Distance>>) _c;

    private static final AggregationProcessorDefinition<Distance, AverageWithStats<Distance>> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Distance.class, _cc, "Average", ParallelDistanceAverageAggregationProcessor.class);
    
    public ParallelDistanceAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, AverageWithStats<Distance>>, ?>> resultReceivers) {
        super(executor, resultReceivers, new ParallelDistanceSumAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelDistanceMinAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()),
                new ParallelDistanceMaxAggregationProcessor(executor, /* resultReceivers */ Collections.emptyList()));
    }

    public static AggregationProcessorDefinition<Distance, AverageWithStats<Distance>> getDefinition() {
        return DEFINITION;
    }

    @Override
    public AggregationProcessorDefinition<Distance, AverageWithStats<Distance>> getProcessorDefinition() {
        return DEFINITION;
    }

    @Override
    protected Distance divide(Distance sum, long elementCount) {
        return sum.scale(1. / (double) elementCount);
    }
}
