package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class DurationDataProvider extends AbstractDataProvider<Duration> {
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
}
