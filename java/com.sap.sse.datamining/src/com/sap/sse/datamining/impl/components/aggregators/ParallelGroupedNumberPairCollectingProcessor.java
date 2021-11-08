package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.PairWithStats;
import com.sap.sse.datamining.shared.impl.PairWithStatsImpl;


@SuppressWarnings("rawtypes")
public class ParallelGroupedNumberPairCollectingProcessor
             extends AbstractParallelGroupedDataStoringAggregationProcessor<Pair, PairWithStats<Number>> {
    
    private static final Class<?> _c = PairWithStats.class;
    @SuppressWarnings("unchecked")
    private static final Class<PairWithStats<Number>> _cc = (Class<PairWithStats<Number>>) _c;
    
    private static final AggregationProcessorDefinition<Pair, PairWithStats<Number>> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Pair.class,
                    _cc, "PairCollecting", ParallelGroupedNumberPairCollectingProcessor.class);
    
    public static AggregationProcessorDefinition<Pair, PairWithStats<Number>> getDefinition() {
        return DEFINITION;
    }
    
    private final Map<GroupKey, HashSet<Pair<Number, Number>>> individualPairs;
    private final Map<GroupKey, AtomicLong> elementAmountPerKey;

    public ParallelGroupedNumberPairCollectingProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, PairWithStats<Number>>, ?>> resultReceivers) {
        super(executor, resultReceivers, "PairCollecting");
        individualPairs = new HashMap<>();
        elementAmountPerKey = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<Pair> element) {
        if (element.getDataEntry() != null) {
            incrementElementAmount(element);
            HashSet<Pair<Number, Number>> aggregate = individualPairs.get(element.getKey());
            Number a = ((Number) element.getDataEntry().getA());
            Number b = ((Number) element.getDataEntry().getB());

            final Double doubleValueA = a == null ? null : a.doubleValue();
            final Double doubleValueB = b == null ? null : b.doubleValue();
            if (doubleValueA != null && doubleValueB != null) {
                if (aggregate == null) {
                    aggregate = new HashSet<>();
                    aggregate.add(new Pair<>(doubleValueA, doubleValueB));
                    individualPairs.put(element.getKey(), aggregate);
                } else {
                    individualPairs.get(element.getKey()).add(new Pair<>(doubleValueA, doubleValueB));
                }
            } 
        }
    }
    
    private void incrementElementAmount(GroupedDataEntry<Pair> element) {
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
    protected Map<GroupKey, PairWithStats<Number>> aggregateResult() {
        Map<GroupKey, PairWithStats<Number>> result = new HashMap<>();
        for (Entry<GroupKey, HashSet<Pair<Number, Number>>> sumAggregationEntry : individualPairs.entrySet()) {
            if (isAborted()) {
                break;
            }
            GroupKey key = sumAggregationEntry.getKey();
            result.put(key, new PairWithStatsImpl<Number>(null,
                    /* min */ null, 
                    /* max */ null,
                    /* median */ null,
                    /* standardDeviation */ null,
                    /* individualPairs */ individualPairs.get(key),
                    /* count */  elementAmountPerKey.get(key).get(),
                    Pair.class.getName()));
        }
        return result;
    }

}
