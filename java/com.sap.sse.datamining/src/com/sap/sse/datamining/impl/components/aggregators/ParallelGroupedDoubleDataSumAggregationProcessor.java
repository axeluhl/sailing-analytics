package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataSumAggregationProcessor
             extends AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Map<GroupKey, Double>> {

    private Map<GroupedDataEntry<Double>, Integer> elementAmountMap;

    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataSumAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Sum");
        elementAmountMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        if (!elementAmountMap.containsKey(element)) {
            elementAmountMap.put(element, 0);
        }
        Integer currentAmount = elementAmountMap.get(element);
        elementAmountMap.put(element, currentAmount + 1);
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        Map<GroupKey, Double> result = new HashMap<>();
        for (Entry<GroupedDataEntry<Double>, Integer> elementAmountEntry : elementAmountMap.entrySet()) {
            Number element = elementAmountEntry.getKey().getDataEntry();
            Integer times = elementAmountEntry.getValue();
            Double multipliedElementValue = multiply(element.doubleValue(), times);
            
            GroupKey groupKey = elementAmountEntry.getKey().getKey();
            Double groupResult = result.get(groupKey);
            result.put(groupKey, addToGroupResult(groupResult, multipliedElementValue));
        }
        return result;
    }

    private Double multiply(Double element, Integer times) {
        return element * times;
    }

    private Double add(Double firstSummand, Double secondSummand) {
        return firstSummand + secondSummand;
    }

    private Double addToGroupResult(Double groupResult, Double multipliedElementValue) {
        if (groupResult == null) {
            return multipliedElementValue;
        }
        return add(groupResult, multipliedElementValue);
    }

}
