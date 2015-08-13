package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataMaxAggregationProcessor
                extends AbstractParallelGroupedDataStoringAggregationProcessor<Double, Double> {
    
    private static final AggregationProcessorDefinition<Double, Double> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Double.class, Double.class, "Maximum", ParallelGroupedDoubleDataMaxAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Double, Double> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Double> maxMap;
    
    public ParallelGroupedDoubleDataMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Maximum");
        maxMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        GroupKey key = element.getKey();
        Double value = element.getDataEntry();
        if (!maxMap.containsKey(key) || value > maxMap.get(key)) {
            maxMap.put(key, value);
        }
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        return maxMap;
    }

}
