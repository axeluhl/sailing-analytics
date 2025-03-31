package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.shared.GroupKey;

public abstract class AbstractParallelComparableMaxAggregationProcessor<T extends Comparable<T>> extends
        AbstractParallelSingleGroupedValueAggregationProcessor<T> {
    
    public AbstractParallelComparableMaxAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, T>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Maximum");
    }

    @Override
    protected T compareValuesAndReturnNewResult(T currentResult, T newValue) {
        return currentResult.compareTo(newValue) >= 0 ? currentResult : newValue;
    }
}
