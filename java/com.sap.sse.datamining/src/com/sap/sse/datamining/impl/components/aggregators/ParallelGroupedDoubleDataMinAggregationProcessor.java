package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataMinAggregationProcessor extends
        AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Double, Map<GroupKey, Double>> {

    private Map<GroupKey, Double> minMap;
    
    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataMinAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
               Double.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Minimum");
        minMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        GroupKey key = element.getKey();
        Double value = element.getDataEntry();
        if (!minMap.containsKey(key) || value < minMap.get(key)) {
            minMap.put(key, value);
        }
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        return minMap;
    }

}
