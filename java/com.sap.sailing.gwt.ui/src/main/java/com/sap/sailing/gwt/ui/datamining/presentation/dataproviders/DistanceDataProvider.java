package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sailing.domain.common.Distance;

public class DistanceDataProvider extends AbstractDataProvider<Distance> {
    
    public DistanceDataProvider() {
        super(Distance.class, getMappings());
    }
    
    private static LinkedHashMap<String, Function<Distance, Number>> getMappings() {
        LinkedHashMap<String, Function<Distance, Number>> mappings = new LinkedHashMap<>();
        mappings.put("Geographical Miles", distance->distance.getGeographicalMiles());
        mappings.put("Sea Miles", distance->distance.getSeaMiles());
        mappings.put("Nautical Miles", distance->distance.getNauticalMiles());
        mappings.put("Meters", distance->distance.getMeters());
        mappings.put("Kilometers", distance->distance.getKilometers());
        mappings.put("Central Angle Degree", distance->distance.getCentralAngleDeg());
        mappings.put("Central Angle Radian", distance->distance.getCentralAngleRad());
        return mappings;
    }
}
