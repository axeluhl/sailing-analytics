package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelTrueSumAggregationProcessor
             extends AbstractParallelGroupedDataStoringAggregationProcessor<Boolean, Double> {
    
    private static final AggregationProcessorDefinition<Boolean, Double> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Boolean.class, Double.class, "Sum", ParallelTrueSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Boolean, Double> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Double> result;

    public ParallelTrueSumAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        result = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Boolean> element) {
        GroupKey key = element.getKey();
        if (!result.containsKey(key)) {
            result.put(key, 0.0);
        }
        Double currentAmount = result.get(key);
        if (element.getDataEntry()) {
            result.put(key, currentAmount + 1);
        }
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        return result;
    }

}
