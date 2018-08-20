package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Distance;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDistanceSumAggregationProcessor extends
        AbstractParallelSumAggregationProcessor<Distance> {
    
    private static final AggregationProcessorDefinition<Distance, Distance> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Distance.class, Distance.class, "Sum", ParallelDistanceSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Distance, Distance> getDefinition() {
        return DEFINITION;
    }
    
    public ParallelDistanceSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, Distance>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Distance add(Distance t1, Distance t2) {
        return t1.add(t2);
    }
}
