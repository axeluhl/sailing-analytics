package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataAverageAggregationProcessor extends
        AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Double, Map<GroupKey, Double>> {

    private final AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Double, Map<GroupKey, Double>> sumAggregationProcessor;
    private final Map<GroupKey, Integer> elementAmountPerKey;

    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
               Double.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        sumAggregationProcessor = new ParallelGroupedDoubleDataSumAggregationProcessor(executor, resultReceivers);
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        incrementElementAmount(element);
        sumAggregationProcessor.storeElement(element);
    }

    private void incrementElementAmount(GroupedDataEntry<Double> element) {
        GroupKey key = element.getKey();
        if (!elementAmountPerKey.containsKey(key)) {
            elementAmountPerKey.put(key, 0);
        }
        Integer currentAmount = elementAmountPerKey.get(key);
        elementAmountPerKey.put(key, currentAmount + 1);
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        Map<GroupKey, Double> result = new HashMap<>();
        Map<GroupKey, Double> sumAggregation = sumAggregationProcessor.aggregateResult();
        for (Entry<GroupKey, Double> sumAggregationEntry : sumAggregation.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, sumAggregationEntry.getValue() / elementAmountPerKey.get(key));
        }
        return result;
    }

}
