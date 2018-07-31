package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMaxAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelComparableMinAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;
import com.sap.sse.datamining.shared.impl.AverageWithStatsImpl;

public abstract class AbstractParallelAverageAggregationProcessor<T extends Comparable<T>>
            extends AbstractParallelGroupedDataStoringAggregationProcessor<T, AverageWithStats<T>> {

    private final AbstractParallelSumAggregationProcessor<T> sumAggregationProcessor;
    private final AbstractParallelComparableMinAggregationProcessor<T> minAggregationProcessor;
    private final AbstractParallelComparableMaxAggregationProcessor<T> maxAggregationProcessor;
    private final Map<GroupKey, Integer> elementAmountPerKey;

    public AbstractParallelAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, AverageWithStats<T>>, ?>> resultReceivers,
            AbstractParallelSumAggregationProcessor<T> sumAggregationProcessor,
            AbstractParallelComparableMinAggregationProcessor<T> minAggregationProcessor,
            AbstractParallelComparableMaxAggregationProcessor<T> maxAggregationProcessor) {
        super(executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        this.sumAggregationProcessor = sumAggregationProcessor;
        this.minAggregationProcessor = minAggregationProcessor;
        this.maxAggregationProcessor = maxAggregationProcessor;
    }

    public abstract AggregationProcessorDefinition<T, AverageWithStats<T>> getProcessorDefinition();

    @Override
    protected void storeElement(GroupedDataEntry<T> element) {
        incrementElementAmount(element);
        sumAggregationProcessor.handleElement(element);
        minAggregationProcessor.handleElement(element);
        maxAggregationProcessor.handleElement(element);
    }

    private void incrementElementAmount(GroupedDataEntry<T> element) {
        GroupKey key = element.getKey();
        if (!elementAmountPerKey.containsKey(key)) {
            elementAmountPerKey.put(key, 0);
        }
        Integer currentAmount = elementAmountPerKey.get(key);
        elementAmountPerKey.put(key, currentAmount + 1);
    }
    
    protected abstract T divide(T sum, long elementCount);

    @Override
    protected Map<GroupKey, AverageWithStats<T>> aggregateResult() {
        Map<GroupKey,  AverageWithStats<T>> result = new HashMap<>();
        Map<GroupKey, T> sumAggregation = sumAggregationProcessor.getResult();
        Map<GroupKey, T> minAggregation = minAggregationProcessor.getResult();
        Map<GroupKey, T> maxAggregation = maxAggregationProcessor.getResult();
        for (Entry<GroupKey, T> sumAggregationEntry : sumAggregation.entrySet()) {
            if (isAborted()) {
                break;
            }
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, new AverageWithStatsImpl<>(
                    /* average */ divide(sumAggregationEntry.getValue(), elementAmountPerKey.get(key).longValue()),
                    /* min */ minAggregation.get(key),
                    /* max */ maxAggregation.get(key),
                    /* median */ null,
                    /* standardDeviation */ null,
                    /* count */ elementAmountPerKey.get(key).longValue(),
                    getProcessorDefinition().getExtractedType().getName()));
        }
        return result;
    }

}
