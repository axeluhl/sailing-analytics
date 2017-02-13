package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sailing.domain.common.Distance;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class DistanceDataProvider extends AbstractDataProvider<Distance> {
    
    private static final String CENTRAL_ANGLE_RADIAN = "Central Angle Radian";
    private static final String CENTRAL_ANGLE_DEGREE = "Central Angle Degree";
    private static final String KILOMETERS = "Kilometers";
    private static final String METERS = "Meters";
    private static final String NAUTICAL_MILES = "Nautical Miles";
    private static final String SEA_MILES = "Sea Miles";
    private static final String GEOGRAPHICAL_MILES = "Geographical Miles";

    public DistanceDataProvider() {
        super(Distance.class, getMappings());
    }
    
    private static LinkedHashMap<String, Function<Distance, Number>> getMappings() {
        LinkedHashMap<String, Function<Distance, Number>> mappings = new LinkedHashMap<>();
        mappings.put(GEOGRAPHICAL_MILES, distance->distance.getGeographicalMiles());
        mappings.put(SEA_MILES, distance->distance.getSeaMiles());
        mappings.put(NAUTICAL_MILES, distance->distance.getNauticalMiles());
        mappings.put(METERS, distance->distance.getMeters());
        mappings.put(KILOMETERS, distance->distance.getKilometers());
        mappings.put(CENTRAL_ANGLE_DEGREE, distance->distance.getCentralAngleDeg());
        mappings.put(CENTRAL_ANGLE_RADIAN, distance->distance.getCentralAngleRad());
        return mappings;
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return METERS;
    }
}
