package com.sap.sse.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelGroupedNumberDataMedianAggregationProcessor
             extends AbstractParallelGroupedDataStoringAggregationProcessor<Number, Number> {
    
    private static final AggregationProcessorDefinition<Number, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class, Number.class, "Median", ParallelGroupedNumberDataMedianAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, Number> getDefinition() {
        return DEFINITION;
    }

    private Map<GroupKey, List<Number>> groupedValues;

    public ParallelGroupedNumberDataMedianAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Median");
        groupedValues = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Number> element) {
        GroupKey key = element.getKey();
        if (!groupedValues.containsKey(key)) {
            groupedValues.put(key, new ArrayList<Number>());
        }
        groupedValues.get(key).add(element.getDataEntry());
    }

    @Override
    protected Map<GroupKey, Number> aggregateResult() {
        Map<GroupKey, Number> result = new HashMap<>();
        for (Entry<GroupKey, List<Number>> groupedValuesEntry : groupedValues.entrySet()) {
            if (isAborted()) {
                break;
            }
            result.put(groupedValuesEntry.getKey(), getMedianOf(groupedValuesEntry.getValue()));
        }
        return result;
    }

    private Double getMedianOf(List<Number> values) {
        Collections.sort(values, new Comparator<Number>() {
            @Override
            public int compare(Number n1, Number n2) {
                return Double.compare(n1.doubleValue(), n2.doubleValue());
            }
        });
        if (listSizeIsEven(values)) {
            int index1 = (values.size() / 2) - 1;
            int index2 = index1 + 1;
            return (values.get(index1).doubleValue() + values.get(index2).doubleValue()) / 2;
        } else {
            int index = ((values.size() + 1) / 2) - 1;
            return values.get(index).doubleValue();
        }
    }

    private boolean listSizeIsEven(List<?> values) {
        return values.size() % 2 == 0;
    }

}
