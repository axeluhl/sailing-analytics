package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public abstract class AbstractParallelGroupedDataStoringAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractParallelGroupedDataAggregationProcessor<InputType, AggregatedType> {

    public AbstractParallelGroupedDataStoringAggregationProcessor(ExecutorService executor,
                                                                  Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers,
                                                                  String aggregationNameMessageKey) {
        super(executor, resultReceivers, aggregationNameMessageKey);
    }

    @Override
    protected void handleElement(GroupedDataEntry<InputType> element) {
        storeElement(element);
    }

    /**
     * Method to store the element in the concrete store. This method is only called in a way, that is thread safe, so
     * that multiple threads can't corrupt the store.
     */
    protected abstract void storeElement(GroupedDataEntry<InputType> element);
    
    @Override
    protected Map<GroupKey, AggregatedType> getResult() {
        return aggregateResult();
    }

    protected abstract Map<GroupKey, AggregatedType> aggregateResult();

}
