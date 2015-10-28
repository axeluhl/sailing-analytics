package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.common.Distance;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDistanceSumAggregationProcessor extends
        AbstractParallelGroupedDataAggregationProcessor<Distance, Distance> {
    
    private static final AggregationProcessorDefinition<Distance, Distance> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Distance.class, Distance.class, "Sum", ParallelDistanceSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Distance, Distance> getDefinition() {
        return DEFINITION;
    }
    
    private final Map<GroupKey, Distance> results;

    public ParallelDistanceSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, Distance>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        results = new HashMap<>();
    }

    @Override
    protected void handleElement(GroupedDataEntry<Distance> element) {
        GroupKey key = element.getKey();
        if (!results.containsKey(key)) {
            results.put(key, element.getDataEntry());
        } else {
            results.put(key, results.get(key).add(element.getDataEntry()));
        }
    }

    @Override
    protected Map<GroupKey, Distance> getResult() {
        return results;
    }

}
