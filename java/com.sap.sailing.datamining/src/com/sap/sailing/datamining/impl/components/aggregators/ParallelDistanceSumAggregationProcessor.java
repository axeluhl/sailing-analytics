package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.shared.dto.DistanceDTO;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public class ParallelDistanceSumAggregationProcessor extends
        AbstractParallelGroupedDataAggregationProcessor<DistanceDTO, DistanceDTO> {
    
    private static final AggregationProcessorDefinition<DistanceDTO, DistanceDTO> DEFINITION =
            new SimpleAggregationProcessorDefinition<>(DistanceDTO.class, DistanceDTO.class, "Sum", ParallelDistanceSumAggregationProcessor.class);
    
    public static AggregationProcessorDefinition<DistanceDTO, DistanceDTO> getDefinition() {
        return DEFINITION;
    }
    
    private final Map<GroupKey, DistanceDTO> results;

    public ParallelDistanceSumAggregationProcessor(ExecutorService executor, Collection<Processor<Map<GroupKey, DistanceDTO>, ?>> resultReceivers) {
        super(executor, resultReceivers, "Sum");
        results = new HashMap<>();
    }

    @Override
    protected void handleElement(GroupedDataEntry<DistanceDTO> element) {
        GroupKey key = element.getKey();
        if (!results.containsKey(key)) {
            results.put(key, element.getDataEntry());
        } else {
            results.put(key, add(results.get(key), element.getDataEntry()));
        }
    }

    private DistanceDTO add(DistanceDTO distance1, DistanceDTO distance2) {
        double geographicalMiles = distance1.getGeographicalMiles() + distance2.getGeographicalMiles();
        double seaMiles = distance1.getSeaMiles() + distance2.getSeaMiles();
        double nauticalMiles = distance1.getNauticalMiles() + distance2.getNauticalMiles();
        double meters = distance1.getMeters() + distance2.getMeters();
        double kilometers = distance1.getKilometers() + distance2.getKilometers();
        double centralAngleDegree = distance1.getCentralAngleDegree() + distance2.getCentralAngleDegree();
        double centralAngleRadian = distance1.getCentralAngleRadian() + distance2.getCentralAngleRadian();
        return new DistanceDTO(geographicalMiles, seaMiles, nauticalMiles, meters, kilometers, centralAngleDegree, centralAngleRadian);
    }

    @Override
    protected Map<GroupKey, DistanceDTO> getResult() {
        return results;
    }

}
