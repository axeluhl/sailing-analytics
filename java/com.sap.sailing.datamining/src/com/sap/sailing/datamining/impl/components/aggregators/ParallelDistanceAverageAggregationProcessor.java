package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDistanceAverageAggregationProcessor
            extends AbstractParallelGroupedDataStoringAggregationProcessor<Distance, Distance> {
    
    private static final AggregationProcessorDefinition<Distance, Distance> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Distance.class, Distance.class, "Average", ParallelDistanceAverageAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Distance, Distance> getDefinition() {
        return DEFINITION;
    }

    private final ParallelDistanceSumAggregationProcessor sumAggregationProcessor;
    private final Map<GroupKey, Integer> elementAmountPerKey;

    public ParallelDistanceAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Distance>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        sumAggregationProcessor = new ParallelDistanceSumAggregationProcessor(executor, resultReceivers);
    }

    @Override
    protected void storeElement(GroupedDataEntry<Distance> element) {
        incrementElementAmount(element);
        sumAggregationProcessor.handleElement(element);
    }

    private void incrementElementAmount(GroupedDataEntry<Distance> element) {
        GroupKey key = element.getKey();
        if (!elementAmountPerKey.containsKey(key)) {
            elementAmountPerKey.put(key, 0);
        }
        Integer currentAmount = elementAmountPerKey.get(key);
        elementAmountPerKey.put(key, currentAmount + 1);
    }

    @Override
    protected Map<GroupKey, Distance> aggregateResult() {
        Map<GroupKey, Distance> result = new HashMap<>();
        Map<GroupKey, Distance> sumAggregation = sumAggregationProcessor.getResult();
        for (Entry<GroupKey, Distance> sumAggregationEntry : sumAggregation.entrySet()) {
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, new MeterDistance(sumAggregationEntry.getValue().getMeters() / elementAmountPerKey.get(key).doubleValue()));
        }
        return result;
    }

}
