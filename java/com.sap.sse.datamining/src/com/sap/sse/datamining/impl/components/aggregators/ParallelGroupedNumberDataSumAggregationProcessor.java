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

public class ParallelGroupedNumberDataSumAggregationProcessor
             extends AbstractParallelGroupedDataAggregationProcessor<Number, Number> {
    
    private static final AggregationProcessorDefinition<Number, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class, Number.class, "Sum", ParallelGroupedNumberDataSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, Number> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Number> result;

    public ParallelGroupedNumberDataSumAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        result = new HashMap<>();
    }
    
    @Override
    protected void handleElement(GroupedDataEntry<Number> element) {
        GroupKey key = element.getKey();
        if (!result.containsKey(key)) {
            result.put(key, element.getDataEntry());
        } else {
            result.put(key, result.get(key).doubleValue() + element.getDataEntry().doubleValue());
        }
    }
    
    @Override
    protected Map<GroupKey, Number> getResult() {
        return result;
    }

}
