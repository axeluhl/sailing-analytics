package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedDoubleDataMaxAggregationProcessor extends
        AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Double, Map<GroupKey, Double>> {

    private Map<GroupKey, Double> maxMap;
    
    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
               Double.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Maximum");
        maxMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
        GroupKey key = element.getKey();
        Double value = element.getDataEntry();
        if (!maxMap.containsKey(key) || value > maxMap.get(key)) {
            maxMap.put(key, value);
        }
    }

    @Override
    protected Map<GroupKey, Double> aggregateResult() {
        return maxMap;
    }

}
