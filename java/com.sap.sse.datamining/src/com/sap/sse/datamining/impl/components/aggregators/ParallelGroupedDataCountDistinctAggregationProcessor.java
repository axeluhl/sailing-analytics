package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.eclipse.jetty.util.ConcurrentHashSet;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;
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
        super(executor, resultReceivers, "Count");
        countMap = new ConcurrentHashMap<>();
    }
    
    /**
     * We don't need synchronization here because we're using a {@link ConcurrentHashSet} and a {@link ConcurrentHashMap} to
     * aggregate the results.
     */
    @Override
    protected AbstractProcessorInstruction<Map<GroupKey, Number>> createInstruction(GroupedDataEntry<Object> element) {
        return new AbstractProcessorInstruction<Map<GroupKey, Number>>(this, ProcessorInstructionPriority.Aggregation) {
            @Override
            public Map<GroupKey, Number> computeResult() {
                handleElement(element);
                return createInvalidResult();
            }
        };
    }

    @Override
    protected void handleElement(GroupedDataEntry<Object> element) {
        GroupKey key = element.getKey();
        Util.addToValueSet(countMap, key, element.getDataEntry(), new Util.ValueSetConstructor<Object>() {
            @Override
            public Set<Object> createSet() {
                return new ConcurrentHashSet<Object>();
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
