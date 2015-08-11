package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.shared.dto.DistanceDTO;

public class DistanceDataProvider extends AbstractResultDataProvider<DistanceDTO> {
    
    private final List<String> dataKeys;

    public DistanceDataProvider() {
        super(DistanceDTO.class);
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
    public Collection<? extends Object> getDataKeys() {
        return dataKeys;
    }

    @Override
    protected Number getData(DistanceDTO distance, Object dataKey) {
        if (!(dataKey instanceof String)) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        String dataKeyString = (String) dataKey;
        switch (dataKeyString) {
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
            return distance.getCentralAngleDegree();
        case "Central Angle Radian":
            return distance.getCentralAngleRadian();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }

}
