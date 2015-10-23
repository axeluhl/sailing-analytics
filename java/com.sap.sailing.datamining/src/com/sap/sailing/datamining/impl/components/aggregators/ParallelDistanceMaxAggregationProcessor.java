package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.common.Distance;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelSingleGroupedValueAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDistanceMaxAggregationProcessor extends
        AbstractParallelSingleGroupedValueAggregationProcessor<Distance> {
    
    private static final AggregationProcessorDefinition<Distance, Distance> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Distance.class, Distance.class, "Minimum", ParallelDistanceMaxAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Distance, Distance> getDefinition() {
        return DEFINITION;
    }

    
    public ParallelDistanceMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Distance>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Minimum");
    }

    @Override
    protected Distance compareValuesAndReturnNewResult(Distance currentResult, Distance newValue) {
        return currentResult.getMeters() >= newValue.getMeters() ? currentResult : newValue;
    }

}
