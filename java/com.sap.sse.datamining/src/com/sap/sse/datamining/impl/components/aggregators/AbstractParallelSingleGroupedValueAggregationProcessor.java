package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * This aggregation processor aggregates a single value per group key.<br>
 * Concrete implementations are for example {@link ParallelGroupedNumberDataMaxAggregationProcessor}
 * and {@link ParallelGroupedNumberDataMinAggregationProcessor}.
 * @author Lennart Hensler (D054527)
 */
public abstract class AbstractParallelSingleGroupedValueAggregationProcessor<ValueType>
                extends AbstractParallelGroupedDataAggregationProcessor<ValueType, ValueType> {

    private Map<GroupKey, ValueType> valueMap;
    
    public AbstractParallelSingleGroupedValueAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, ValueType>, ?>> resultReceivers, String aggregationNameMessageKey) {
        super(executor, resultReceivers, aggregationNameMessageKey);
        valueMap = new HashMap<>();
    }

    @Override
    public void handleElement(GroupedDataEntry<ValueType> element) {
        GroupKey key = element.getKey();
        ValueType value = element.getDataEntry();
        if (value != null) {
            if (!valueMap.containsKey(key)) {
                valueMap.put(key, value);
            } else {
                valueMap.put(key, compareValuesAndReturnNewResult(valueMap.get(key), value));
            }
        }
    }

    /**
     * @param currentResult not {@code null}
     * @param newValue not {@code null}
     * @return not {@code null}
     */
    protected abstract ValueType compareValuesAndReturnNewResult(ValueType currentResult, ValueType newValue);

    @Override
    public Map<GroupKey, ValueType> getResult() {
        return valueMap;
    }

}
