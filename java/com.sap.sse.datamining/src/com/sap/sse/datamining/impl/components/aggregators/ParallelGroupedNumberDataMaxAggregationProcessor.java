package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedNumberDataMaxAggregationProcessor
                extends AbstractParallelSingleGroupedValueAggregationProcessor<Number> {
    
    private static final AggregationProcessorDefinition<Number, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class, Number.class, "Maximum", ParallelGroupedNumberDataMaxAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, Number> getDefinition() {
        return DEFINITION;
    }

    public ParallelGroupedNumberDataMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Maximum");
    }

    @Override
    protected Number compareValuesAndReturnNewValue(Number previousValue, Number newValue) {
        return previousValue.doubleValue() >= newValue.doubleValue() ? previousValue : newValue;
    }

}
