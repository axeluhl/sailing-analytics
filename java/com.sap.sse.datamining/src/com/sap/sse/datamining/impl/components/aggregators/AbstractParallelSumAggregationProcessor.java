package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractParallelSumAggregationProcessor<InputType, AggregatedType>
                      extends AbstractStoringParallelAggregationProcessor<InputType, AggregatedType> {

    private Map<InputType, Integer> elementAmountMap;

    public AbstractParallelSumAggregationProcessor(Executor executor, Collection<Processor<AggregatedType>> resultReceivers) {
        super(executor, resultReceivers);
        elementAmountMap = new HashMap<>();
    }

    @Override
    protected void storeElement(InputType element) {
        if (!elementAmountMap.containsKey(element)) {
            elementAmountMap.put(element, 0);
        }
        Integer currentAmount = elementAmountMap.get(element);
        elementAmountMap.put(element, currentAmount + 1);
    }
    
    protected Map<InputType, Integer> getElementAmountMap() {
        return elementAmountMap;
    }

}
