package com.sap.sailing.gwt.home.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;

/**
 * {@link RaceCallback} implementation, which prepares races information to be displayed in the competition format.
 * Therefore, it builds a hierarchy of DTOs representing the {@link RaceCompetitionFormatSeriesDTO series},
 * {@link RaceCompetitionFormatFleetDTO fleets} and {@link SimpleRaceMetadataDTO races} of a regatta.
 */
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
    
    /**
     * @return collection of {@link RaceCompetitionFormatSeriesDTO}s representing the regattas hierarchy, containing the
     *         various series, fleets and races.
     */
    public Collection<RaceCompetitionFormatSeriesDTO> getResult() {
        Set<RaceCompetitionFormatSeriesDTO> result = new LinkedHashSet<>();
        for (Entry<String, Map<FleetMetadataDTO, SeriesFleetData>> seriesData : dataPerSeriesAndFleet.entrySet()) {
            RaceCompetitionFormatSeriesDTO series = new RaceCompetitionFormatSeriesDTO(seriesData.getKey());
            Set<SimpleCompetitorDTO> competitorsForSeries = new HashSet<>();
            int sumOfSeparatCompetitorsInFleets = 0;
            for (Entry<FleetMetadataDTO, SeriesFleetData> fleetData : seriesData.getValue().entrySet()) {
                competitorsForSeries.addAll(fleetData.getValue().competitors);
                sumOfSeparatCompetitorsInFleets += fleetData.getValue().competitors.size();
            }
            series.setCompetitorCount(competitorsForSeries.size());
            // if competitors change the fleets race by race, they can't be associated to a single fleet
            // Therefore, the competitors by fleet statistic will be hidden to avoid confusing information
            boolean showCompetitorCountForFleet = series.getCompetitorCount() == sumOfSeparatCompetitorsInFleets;
            for (Entry<FleetMetadataDTO, SeriesFleetData> fleetData : seriesData.getValue().entrySet()) {
                int competitorCountForFleet = showCompetitorCountForFleet ? fleetData.getValue().competitors.size() : 0;
                RaceCompetitionFormatFleetDTO fleet = new RaceCompetitionFormatFleetDTO(fleetData.getKey(), competitorCountForFleet);
                fleet.addRaces(fleetData.getValue().races);
                series.addFleet(fleet);
            }
            result.add(series);
        }
        return result;
    }
    
    private class SeriesFleetData {
        private final Set<SimpleRaceMetadataDTO> races = new LinkedHashSet<>();
        private final Set<SimpleCompetitorDTO> competitors = new HashSet<>();
    }

}
