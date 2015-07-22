package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

public class RaceCompetitionFormatDataCalculator implements RaceCallback {
    
    private final Map<String, RaceCompetitionFormatSeriesDTO> seriesMap = new HashMap<>();

    @Override
    public void doForRace(RaceContext context) {
        RaceCompetitionFormatSeriesDTO series = ensureSeries(context.getSeriesName());
        series.addRace(context.getFleetMetadata(), context.getRaceCompetitionFormat());
    }
    
    private RaceCompetitionFormatSeriesDTO ensureSeries(String seriesName) {
        RaceCompetitionFormatSeriesDTO series = seriesMap.get(seriesName);
        if (series == null) {
            seriesMap.put(seriesName, series = new RaceCompetitionFormatSeriesDTO(seriesName));
        }
        return series;
    }
    
    public Collection<RaceCompetitionFormatSeriesDTO> getResult() {
        return seriesMap.values();
    }

}
