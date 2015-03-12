package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.Unit;

public class ParallelGroupedDoubleDataCountAggregationProcessor extends
        AbstractParallelStoringAggregationProcessor<GroupedDataEntry<Double>, Map<GroupKey, Double>> {

    private Map<GroupKey, Double> countMap;
    
    @SuppressWarnings("unchecked")
    public ParallelGroupedDoubleDataCountAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers) {
        super((Class<GroupedDataEntry<Double>>)(Class<?>) GroupedDataEntry.class,
              (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
              executor, resultReceivers, "Count");
        countMap = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Double> element) {
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
