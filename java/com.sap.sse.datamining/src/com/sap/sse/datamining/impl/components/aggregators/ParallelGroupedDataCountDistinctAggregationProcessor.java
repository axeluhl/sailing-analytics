package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * Counts distinct instances; for this, the aggregator needs to keep in memory a {@link Set} of the
 * group-keyed objects to match new objects with already existing ones based on their {@link Object#equals(Object)}
 * and {@link Object#hashCode()} definitions.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ParallelGroupedDataCountDistinctAggregationProcessor
                extends AbstractParallelGroupedDataAggregationProcessor<Object, Number> {
    
    private static final AggregationProcessorDefinition<Object, Number> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Object.class, Number.class, "CountDistinct", ParallelGroupedDataCountDistinctAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Object, Number> getDefinition() {
        return DEFINITION;
    }

    private ConcurrentHashMap<GroupKey, Set<Object>> countMap;
    
    public ParallelGroupedDataCountDistinctAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, Number>, ?>> resultReceivers) {
        super(executor, resultReceivers, "CountDistinct");
        countMap = new ConcurrentHashMap<>();
    }

    @Override
    protected boolean needsSynchronization() {
        return false;
    }

    @Override
    protected void handleElement(GroupedDataEntry<Object> element) {
        GroupKey key = element.getKey();
        Util.addToValueSet(countMap, key, element.getDataEntry(), new Util.ValueCollectionConstructor<Object, Set<Object>>() {
            @Override
            public Set<Object> createValueCollection() {
                return ConcurrentHashMap.newKeySet();
            }
        });
    }
    
    @Override
    protected Map<GroupKey, Number> getResult() {
        final Map<GroupKey, Number> result = new HashMap<>();
        for (final Entry<GroupKey, Set<Object>> e : countMap.entrySet()) {
            result.put(e.getKey(), e.getValue().size());
        }
        return result;
    }
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        super.setAdditionalData(additionalDataBuilder);
        additionalDataBuilder.setResultDecimals(0);
    }

}
