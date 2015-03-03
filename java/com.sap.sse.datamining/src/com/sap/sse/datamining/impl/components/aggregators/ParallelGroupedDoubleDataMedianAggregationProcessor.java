package com.sap.sse.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataMedianAggregationProcessor
             extends AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Map<GroupKey, Double>> {

    private Map<GroupKey, List<Double>> groupedValues;

    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataMedianAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Median");
        groupedValues = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        GroupKey key = element.getKey();
        if (!groupedValues.containsKey(key)) {
            groupedValues.put(key, new ArrayList<Double>());
        }
        groupedValues.get(key).add(element.getDataEntry());
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        Map<GroupKey, Double> result = new HashMap<>();
        for (Entry<GroupKey, List<Double>> groupedValuesEntry : groupedValues.entrySet()) {
            result.put(groupedValuesEntry.getKey(), getMedianOf(groupedValuesEntry.getValue()));
        }
        return result;
    }

    private Double getMedianOf(List<Double> values) {
        Collections.sort(values);
        if (listSizeIsEven(values)) {
            int index1 = (values.size() / 2) - 1;
            int index2 = index1 + 1;
            return (values.get(index1) + values.get(index2)) / 2;
        } else {
            int index = ((values.size() + 1) / 2) - 1;
            return values.get(index);
        }
    }

    private boolean listSizeIsEven(List<Double> values) {
        return values.size() % 2 == 0;
    }

}
