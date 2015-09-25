package com.sap.sailing.polars.datamining.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sailing.polars.datamining.shared.PolarAggregationImpl;
import com.sap.sailing.polars.datamining.shared.PolarStatistic;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * Keeps one {@link PolarAggregation} per group key and incrementally updates that aggregation when elements are added.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public class PolarDataAggregationProcessor extends AbstractParallelGroupedDataAggregationProcessor<PolarStatistic, PolarAggregation> {

    private static final String POLARS_MESSAGE_KEY = "Polars";
    private final Map<GroupKey, PolarAggregation> resultMap = new HashMap<>();
    
    public PolarDataAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, PolarAggregation>, ?>> resultReceivers) {
        super(executor, resultReceivers, POLARS_MESSAGE_KEY);
    }
    
    private static final AggregationProcessorDefinition<PolarStatistic, PolarAggregation> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(PolarStatistic.class, PolarAggregation.class, POLARS_MESSAGE_KEY, PolarDataAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<PolarStatistic, PolarAggregation> getDefinition() {
        return DEFINITION;
    }

    @Override
    protected void handleElement(GroupedDataEntry<PolarStatistic> element) {
        PolarAggregation polarAggregation = resultMap.get(element.getKey());
        if (polarAggregation == null) {
            polarAggregation = new PolarAggregationImpl(element.getDataEntry().getSettings());
            resultMap.put(element.getKey(), polarAggregation);
        }
        polarAggregation.addElement(element.getDataEntry());
    }

    @Override
    protected Map<GroupKey, PolarAggregation> getResult() {
        return resultMap;
    }

}
