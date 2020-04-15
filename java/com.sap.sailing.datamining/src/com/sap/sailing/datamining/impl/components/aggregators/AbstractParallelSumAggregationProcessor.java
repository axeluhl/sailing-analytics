package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public abstract class AbstractParallelSumAggregationProcessor<T> extends
        AbstractParallelGroupedDataAggregationProcessor<T, T> {
    
    private final Map<GroupKey, T> results;

    public AbstractParallelSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, T>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        results = new HashMap<>();
    }

    @Override
    protected void handleElement(GroupedDataEntry<T> element) {
        GroupKey key = element.getKey();
        if (!results.containsKey(key)) {
            results.put(key, element.getDataEntry());
        } else {
            results.put(key, add(results.get(key), element.getDataEntry()));
        }
    }
    
    protected abstract T add(T t1, T t2);

    @Override
    protected Map<GroupKey, T> getResult() {
        return results;
    }
}
