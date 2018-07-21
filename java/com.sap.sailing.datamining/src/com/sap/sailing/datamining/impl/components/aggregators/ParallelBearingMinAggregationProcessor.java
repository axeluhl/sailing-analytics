package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Bearing;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMinAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelBearingMinAggregationProcessor extends
    AbstractParallelComparableMinAggregationProcessor<Bearing> {
    
    private static final AggregationProcessorDefinition<Bearing, Bearing> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Bearing.class, Bearing.class, "Minimum", ParallelBearingMinAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Bearing, Bearing> getDefinition() {
        return DEFINITION;
    }

    
    public ParallelBearingMinAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Bearing>, ?>> resultReceivers) {
        super(executor, resultReceivers);
    }
}
