package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.common.Speed;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

public class SpeedDataProvider extends AbstractNumericDataProviderWithStaticMappings<Speed> {

    private static final String BEAUFORT = "Beaufort";
    private static final String KILOMETERS_PER_HOUR = "KilometersPerHour";
    private static final String KNOTS = "Knots";
    private static final String METERS_PER_SECOND = "MetersPerSecond";
    private static final String STATUTE_MILES_PER_HOUR = "StatuteMilesPerHour";

    public SpeedDataProvider() {
        super(Speed.class, getMappings());
    }

    private static LinkedHashMap<String, Function<Speed, Number>> getMappings() {
        LinkedHashMap<String, Function<Speed, Number>> mappings = new LinkedHashMap<>();
        mappings.put(BEAUFORT, speed -> speed.getBeaufort());
        mappings.put(KILOMETERS_PER_HOUR, speed -> speed.getKilometersPerHour());
        mappings.put(KNOTS, speed -> speed.getKnots());
        mappings.put(METERS_PER_SECOND, speed -> speed.getMetersPerSecond());
        mappings.put(STATUTE_MILES_PER_HOUR, speed -> speed.getStatuteMilesPerHour());
        return mappings;
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return KNOTS;
    }

    @Override
    public String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages, String dataKey) {
        switch (dataKey) {
        case BEAUFORT:
            return stringMessages.beaufort();
        case KILOMETERS_PER_HOUR:
            return stringMessages.kilometersPerHour();
        case KNOTS:
            return stringMessages.knots();
        case METERS_PER_SECOND:
            return stringMessages.metersPerSecond();
        case STATUTE_MILES_PER_HOUR:
            return stringMessages.statuteMilesPerHour();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
}
