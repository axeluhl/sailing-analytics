package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDurationAverageAggregationProcessor
            extends AbstractParallelGroupedDataStoringAggregationProcessor<Duration, Duration> {
    
    private static final AggregationProcessorDefinition<Duration, Duration> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, Duration.class, "Average", ParallelDurationAverageAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, Duration> getDefinition() {
        return DEFINITION;
    }

    private final ParallelDurationSumAggregationProcessor sumAggregationProcessor;
    private final Map<GroupKey, Integer> elementAmountPerKey;

    public ParallelDurationAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Duration>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        sumAggregationProcessor = new ParallelDurationSumAggregationProcessor(executor, resultReceivers);
    }

    @Override
    protected void storeElement(GroupedDataEntry<Duration> element) {
        incrementElementAmount(element);
        sumAggregationProcessor.handleElement(element);
    }

    private void incrementElementAmount(GroupedDataEntry<Duration> element) {
        GroupKey key = element.getKey();
        if (!elementAmountPerKey.containsKey(key)) {
            elementAmountPerKey.put(key, 0);
        }
        Integer currentAmount = elementAmountPerKey.get(key);
        elementAmountPerKey.put(key, currentAmount + 1);
    }

    @Override
    protected Map<GroupKey, Duration> aggregateResult() {
        Map<GroupKey, Duration> result = new HashMap<>();
        Map<GroupKey, Duration> sumAggregation = sumAggregationProcessor.getResult();
        for (Entry<GroupKey, Duration> sumAggregationEntry : sumAggregation.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, sumAggregationEntry.getValue().divide(elementAmountPerKey.get(key).doubleValue()));
        }
        return result;
    }

}
