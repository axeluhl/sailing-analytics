package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.omg.CORBA.DoubleHolder;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;
import com.sap.sse.datamining.shared.impl.AverageWithStatsImpl;

public class ParallelGroupedNumberDataAverageAggregationProcessor
            extends AbstractParallelGroupedDataStoringAggregationProcessor<Number, AverageWithStats<Number>> {
    private static final Class<?> _c = AverageWithStats.class;
    @SuppressWarnings("unchecked")
    private static final Class<AverageWithStats<Number>> _cc = (Class<AverageWithStats<Number>>) _c;
    
    private static final AggregationProcessorDefinition<Number, AverageWithStats<Number>> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Number.class,
                    _cc, "Average", ParallelGroupedNumberDataAverageAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Number, AverageWithStats<Number>> getDefinition() {
        return DEFINITION;
    }

    private final Map<GroupKey, DoubleHolder> sumPerKey;
    private final Map<GroupKey, Double> minPerKey;
    private final Map<GroupKey, Double> maxPerKey;
    private final Map<GroupKey, AtomicLong> elementAmountPerKey;

    public ParallelGroupedNumberDataAverageAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, AverageWithStats<Number>>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Average");
        elementAmountPerKey = new HashMap<>();
        sumPerKey = new HashMap<>();
        minPerKey = new HashMap<>();
        maxPerKey = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Number> element) {
        if (element.getDataEntry() != null) {
            incrementElementAmount(element);
            // concurrency is not an issue here; needsSynchronization() returns true
            DoubleHolder aggregate = sumPerKey.get(element.getKey());
            final double doubleValue = element.getDataEntry().doubleValue();
            if (aggregate == null) {
                aggregate = new DoubleHolder(doubleValue);
                sumPerKey.put(element.getKey(), aggregate);
            } else {
                aggregate.value += doubleValue;
            }
            if (!minPerKey.containsKey(element.getKey()) || doubleValue < minPerKey.get(element.getKey())) {
                minPerKey.put(element.getKey(), doubleValue);
            }
            if (!maxPerKey.containsKey(element.getKey()) || doubleValue > maxPerKey.get(element.getKey())) {
                maxPerKey.put(element.getKey(), doubleValue);
            }
        }
    }

    private void incrementElementAmount(GroupedDataEntry<Number> element) {
        GroupKey key = element.getKey();
        // concurrency is not an issue here; needsSynchronization() returns true
        AtomicLong currentAmount = elementAmountPerKey.get(key);
        if (currentAmount == null) {
            elementAmountPerKey.put(key, new AtomicLong(1));
        } else {
            currentAmount.incrementAndGet();
        }
    }

    @Override
    protected Map<GroupKey, AverageWithStats<Number>> aggregateResult() {
        Map<GroupKey, AverageWithStats<Number>> result = new HashMap<>();
        for (Entry<GroupKey, DoubleHolder> sumAggregationEntry : sumPerKey.entrySet()) {
            if (isAborted()) {
                break;
            }
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, new AverageWithStatsImpl<Number>(sumAggregationEntry.getValue().value / elementAmountPerKey.get(key).get(),
                    minPerKey.get(key), maxPerKey.get(key),
                    /* median */ null,
                    /* standardDeviation */ null,
                    /* count */ elementAmountPerKey.get(key).get(),
                    Number.class.getName()));
        }
        return result;
    }

}
