package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDataCountAggregationProcessor
                extends AbstractParallelGroupedDataAggregationProcessor<Object, Number> {
    
    private static final AggregationProcessorDefinition<Object, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Object.class, Number.class, "Count", ParallelGroupedDataCountAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Object, Number> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Number> countMap;
    
    public ParallelGroupedDataCountAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Count");
        countMap = new HashMap<>();
    }
    
    @Override
    protected void handleElement(GroupedDataEntry<Object> element) {
        GroupKey key = element.getKey();
        if (!countMap.containsKey(key)) {
            countMap.put(key, 0.0);
        }
        Double currentAmount = countMap.get(key).doubleValue();
        countMap.put(key, currentAmount + 1.0);
    }
    
    @Override
    protected Map<GroupKey, Number> getResult() {
        return countMap;
    }
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        super.setAdditionalData(additionalDataBuilder);
        additionalDataBuilder.setResultDecimals(0);
    }

}
