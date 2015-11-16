package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.Distance;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class DistanceDataProvider extends AbstractResultDataProvider<Distance> {
    
    private final List<String> dataKeys;

    public DistanceDataProvider() {
        super(Distance.class);
        dataKeys = new ArrayList<>();
        dataKeys.add("Geographical Miles");
        dataKeys.add("Sea Miles");
        dataKeys.add("Nautical Miles");
        dataKeys.add("Meters");
        dataKeys.add("Kilometers");
        dataKeys.add("Central Angle Degree");
        dataKeys.add("Central Angle Radian");
    }

    @Override
    public Collection<String> getDataKeys() {
        return dataKeys;
    }
    
    @Override
    public boolean acceptsResultsOfType(String type) {
        return getResultType().getName().equals(type);
    }

    @Override
    protected Number getData(Distance distance, String dataKey) {
        switch (dataKey) {
        case "Geographical Miles":
            return distance.getGeographicalMiles();
        case "Sea Miles":
            return distance.getSeaMiles();
        case "Nautical Miles":
            return distance.getNauticalMiles();
        case "Meters":
            return distance.getMeters();
        case "Kilometers":
            return distance.getKilometers();
        case "Central Angle Degree":
            return distance.getCentralAngleDeg();
        case "Central Angle Radian":
            return distance.getCentralAngleRad();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
    
    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return dataKeys.get(0);
    }

}
