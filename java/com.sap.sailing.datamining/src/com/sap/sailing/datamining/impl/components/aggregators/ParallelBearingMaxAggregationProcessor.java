package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Bearing;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMaxAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelBearingMaxAggregationProcessor extends
    AbstractParallelComparableMaxAggregationProcessor<Bearing> {
    
    private static final AggregationProcessorDefinition<Bearing, Bearing> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Bearing.class, Bearing.class, "Maximum", ParallelBearingMaxAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Bearing, Bearing> getDefinition() {
        return DEFINITION;
    }

    
    public ParallelBearingMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Bearing>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }
}
