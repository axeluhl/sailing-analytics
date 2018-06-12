package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.common.Bearing;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

public class BearingDataProvider extends AbstractNumericDataProviderWithStaticMappings<Bearing> {

    private static final String DEGREES = "Degrees";
    private static final String RADIANS = "Radians";

    public BearingDataProvider() {
        super(Bearing.class, getMappings());
    }

    private static LinkedHashMap<String, Function<Bearing, Number>> getMappings() {
        LinkedHashMap<String, Function<Bearing, Number>> mappings = new LinkedHashMap<>();
        mappings.put(DEGREES, bearing -> bearing.getDegrees());
        mappings.put(RADIANS, bearing -> bearing.getRadians());
        return mappings;
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return DEGREES;
    }

    @Override
    public String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages, String dataKey) {
        switch (dataKey) {
        case DEGREES:            
            return stringMessages.angleInDegree();
        case RADIANS:
            return stringMessages.angleInRadian();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
}
