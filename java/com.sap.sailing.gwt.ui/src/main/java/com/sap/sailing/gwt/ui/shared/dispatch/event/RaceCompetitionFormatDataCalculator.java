package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;

public class RaceCompetitionFormatDataCalculator implements RaceCallback {
    
    private final Map<String, Map<FleetMetadataDTO, SeriesFleetData>> dataPerSeriesAndFleet = new LinkedHashMap<>();

    @Override
    public void doForRace(RaceContext context) {
        SeriesFleetData seriesFleetData = getSeriesFleetData(context.getSeriesName(), context.getFleetMetadata());
        seriesFleetData.races.add(context.getRaceCompetitionFormat());
        seriesFleetData.competitors.addAll(context.getCompetitors());
    }
    
    private SeriesFleetData getSeriesFleetData(String seriesName, FleetMetadataDTO fleetMetadata) {
        Map<FleetMetadataDTO, SeriesFleetData> dataForSeriesPerFleet = dataPerSeriesAndFleet.get(seriesName);
        if (dataForSeriesPerFleet == null) {
            dataPerSeriesAndFleet.put(seriesName, dataForSeriesPerFleet = new TreeMap<>());
        }
        SeriesFleetData seriesFleetData = dataForSeriesPerFleet.get(fleetMetadata);
        if (seriesFleetData == null) {
            dataForSeriesPerFleet.put(fleetMetadata, seriesFleetData = new SeriesFleetData());
        }
        return seriesFleetData;
    }
    
    public Collection<RaceCompetitionFormatSeriesDTO> getResult() {
        Set<RaceCompetitionFormatSeriesDTO> result = new LinkedHashSet<>();
        for (Entry<String, Map<FleetMetadataDTO, SeriesFleetData>> seriesData : dataPerSeriesAndFleet.entrySet()) {
            RaceCompetitionFormatSeriesDTO series = new RaceCompetitionFormatSeriesDTO(seriesData.getKey());
            Set<SimpleCompetitorDTO> competitorsForSeries = new HashSet<>();
            for (Entry<FleetMetadataDTO, SeriesFleetData> fleetData : seriesData.getValue().entrySet()) {
                competitorsForSeries.addAll(fleetData.getValue().competitors);
                int competitorCountForFleet = fleetData.getValue().competitors.size();
                RaceCompetitionFormatFleetDTO fleet = new RaceCompetitionFormatFleetDTO(fleetData.getKey(), competitorCountForFleet);
                fleet.addRaces(fleetData.getValue().races);
                series.addFleet(fleet);
            }
            series.setCompetitorCount(competitorsForSeries.size());
            result.add(series);
        }
        return result;
    }
    
    private class SeriesFleetData {
        private final Set<SimpleRaceMetadataDTO> races = new HashSet<>();
        private final Set<SimpleCompetitorDTO> competitors = new HashSet<>();
    }

}
