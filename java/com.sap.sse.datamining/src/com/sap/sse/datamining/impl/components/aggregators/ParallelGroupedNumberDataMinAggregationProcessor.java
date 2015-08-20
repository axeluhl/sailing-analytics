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

public class ParallelGroupedNumberDataMinAggregationProcessor
                extends AbstractParallelGroupedDataAggregationProcessor<Number, Number> {
    
    private static final AggregationProcessorDefinition<Number, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class, Number.class, "Minimum", ParallelGroupedNumberDataMinAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, Number> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Number> minMap;
    
    public ParallelGroupedNumberDataMinAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Minimum");
        minMap = new HashMap<>();
    }

    @Override
    protected void handleElement(GroupedDataEntry<Number> element) {
        GroupKey key = element.getKey();
        Number value = element.getDataEntry();
        if (!minMap.containsKey(key) || value.doubleValue() < minMap.get(key).doubleValue()) {
            minMap.put(key, value);
        }
    }

    @Override
    protected Map<GroupKey, Number> getResult() {
        return minMap;
    }

}
