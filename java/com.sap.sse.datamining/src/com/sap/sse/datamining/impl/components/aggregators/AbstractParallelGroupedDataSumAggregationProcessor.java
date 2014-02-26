package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public abstract class AbstractParallelGroupedDataSumAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractParallelSumAggregationProcessor<GroupedDataEntry<InputType>, Map<GroupKey, AggregatedType>> {

    public AbstractParallelGroupedDataSumAggregationProcessor(Executor executor,
            Collection<Processor<Map<GroupKey, AggregatedType>>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Map<GroupKey, AggregatedType> aggregateResult() {
        Map<GroupKey, AggregatedType> result = new HashMap<>();
        for (Entry<GroupedDataEntry<InputType>, Integer> elementAmountEntry : getElementAmountMap().entrySet()) {
            InputType element = elementAmountEntry.getKey().getDataEntry();
            Integer times = elementAmountEntry.getValue();
            AggregatedType multipliedElementValue = multiply(element, times);
            
            GroupKey groupKey = elementAmountEntry.getKey().getKey();
            AggregatedType groupResult = result.get(groupKey);
            result.put(groupKey, addToGroupResult(groupResult, multipliedElementValue));
        }
        return result;
    }

    private AggregatedType addToGroupResult(AggregatedType groupResult, AggregatedType multipliedElementValue) {
        if (groupResult == null) {
            return multipliedElementValue;
        }
        return add(groupResult, multipliedElementValue);
    }

    protected abstract AggregatedType multiply(InputType element, Integer times);

    protected abstract AggregatedType add(AggregatedType firstSummand, AggregatedType secondSummand);

}
