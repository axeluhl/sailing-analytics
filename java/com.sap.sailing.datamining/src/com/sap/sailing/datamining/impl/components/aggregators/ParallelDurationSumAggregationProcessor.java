package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDurationSumAggregationProcessor extends
        AbstractParallelGroupedDataAggregationProcessor<Duration, Duration> {
    
    private static final AggregationProcessorDefinition<Duration, Duration> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(Duration.class, Duration.class, "Sum", ParallelDurationSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<Duration, Duration> getDefinition() {
        return DEFINITION;
    }
    
    private final Map<GroupKey, Duration> results;

    public ParallelDurationSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, Duration>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        results = new HashMap<>();
    }

    @Override
    protected void handleElement(GroupedDataEntry<Duration> element) {
        GroupKey key = element.getKey();
        if (!results.containsKey(key)) {
            results.put(key, element.getDataEntry());
        } else {
            results.put(key, results.get(key).plus(element.getDataEntry()));
        }
    }

    @Override
    protected Map<GroupKey, Duration> getResult() {
        return results;
    }
}
