package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedNumberDataAverageAggregationProcessor
            extends AbstractParallelGroupedDataStoringAggregationProcessor<Number, Number> {
    
    private static final AggregationProcessorDefinition<Number, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class, Number.class, "Average", ParallelGroupedNumberDataAverageAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, Number> getDefinition() {
        return DEFINITION;
    }

    private final ParallelGroupedNumberDataSumAggregationProcessor sumAggregationProcessor;
    private final Map<GroupKey, Integer> elementAmountPerKey;

    public ParallelGroupedNumberDataAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        sumAggregationProcessor = new ParallelGroupedNumberDataSumAggregationProcessor(executor, resultReceivers);
    }

    @Override
    protected void storeElement(GroupedDataEntry<Number> element) {
        incrementElementAmount(element);
        sumAggregationProcessor.handleElement(element);
    }

    private void incrementElementAmount(GroupedDataEntry<Number> element) {
        GroupKey key = element.getKey();
        if (!elementAmountPerKey.containsKey(key)) {
            elementAmountPerKey.put(key, 0);
        }
        Integer currentAmount = elementAmountPerKey.get(key);
        elementAmountPerKey.put(key, currentAmount + 1);
    }

    @Override
    protected Map<GroupKey, Number> aggregateResult() {
        Map<GroupKey, Number> result = new HashMap<>();
        Map<GroupKey, Number> sumAggregation = sumAggregationProcessor.getResult();
        for (Entry<GroupKey, Number> sumAggregationEntry : sumAggregation.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, sumAggregationEntry.getValue().doubleValue() / elementAmountPerKey.get(key).doubleValue());
        }
        return result;
    }

}
