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
import com.sap.sse.datamining.shared.data.Unit;

public class ParallelGroupedDataCountAggregationProcessor
                extends AbstractParallelGroupedDataStoringAggregationProcessor<Object, Double> {
    
    private static final AggregationProcessorDefinition<Object, Double> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Object.class, Double.class, "Count", ParallelGroupedDataCountAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Object, Double> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, Double> countMap;
    
    public ParallelGroupedDataCountAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Count");
        countMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Object> element) {
        GroupKey key = element.getKey();
        if (!countMap.containsKey(key)) {
            countMap.put(key, 0.0);
        }
        Double currentAmount = countMap.get(key);
        countMap.put(key, currentAmount + 1.0);
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        return countMap;
    }
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        super.setAdditionalData(additionalDataBuilder);
        additionalDataBuilder.setResultUnit(Unit.None);
        additionalDataBuilder.setResultDecimals(0);
    }

}
