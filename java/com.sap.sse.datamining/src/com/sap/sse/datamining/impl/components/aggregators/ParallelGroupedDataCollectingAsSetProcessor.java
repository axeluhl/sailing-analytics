package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * An aggregator, that stores the received elements in Sets mapped by the {@link GroupKey GroupKeys}.<br>
 * The aggregated result is a <code>{@literal Map<GroupKey, Set<DataType>>}</code>.
 * 
 * @author Lennart
 *
 * @param <DataType> The data type for the elements in the resulting sets
 */
public class ParallelGroupedDataCollectingAsSetProcessor<DataType>
             extends AbstractParallelGroupedDataStoringAggregationProcessor<DataType, HashSet<DataType>> {

    private final Map<GroupKey, HashSet<DataType>> collectedDataMappedByGroupKey;
    
    public ParallelGroupedDataCollectingAsSetProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, HashSet<DataType>>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Collecting");
        collectedDataMappedByGroupKey = new HashMap<>();
    }

    @Override
    protected void storeElement(GroupedDataEntry<DataType> element) {
        if (!collectedDataMappedByGroupKey.containsKey(element.getKey())) {
            collectedDataMappedByGroupKey.put(element.getKey(), new HashSet<DataType>());
        }
        collectedDataMappedByGroupKey.get(element.getKey()).add(element.getDataEntry());
    }

    @Override
    protected Map<GroupKey, HashSet<DataType>> aggregateResult() {
        return collectedDataMappedByGroupKey;
    }

}
