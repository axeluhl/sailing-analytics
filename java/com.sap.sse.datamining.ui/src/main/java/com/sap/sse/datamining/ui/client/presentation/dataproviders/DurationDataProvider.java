package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

public class DurationDataProvider extends AbstractNumericDataProviderWithStaticMappings<Duration> {
    private static final String DAYS = "Days";
    private static final String HOURS = "Hours";
    private static final String MINUTES = "Minutes";
    private static final String SECONDS = "Seconds";
    private static final String MILLISECONDS = "Milliseconds";

    public DurationDataProvider() {
        super(Duration.class, getMappings());
    }

    private static LinkedHashMap<String, Function<Duration, Number>> getMappings() {
        LinkedHashMap<String, Function<Duration, Number>> mappings = new LinkedHashMap<>();
        mappings.put(MILLISECONDS, duration->duration.asMillis());
        mappings.put(SECONDS, duration->duration.asSeconds());
        mappings.put(MINUTES, duration->duration.asMinutes());
        mappings.put(HOURS, duration->duration.asHours());
        mappings.put(DAYS, duration->duration.asDays());
        return mappings;
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return SECONDS;
    }

    @Override
    public String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages, String dataKey) {
        switch (dataKey) {
        case DAYS:
            return stringMessages.days();
        case HOURS:
            return stringMessages.hours();
        case MINUTES:
            return stringMessages.minutes();
        case SECONDS:
            return stringMessages.seconds();
        case MILLISECONDS:
            return stringMessages.milliseconds();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
}
