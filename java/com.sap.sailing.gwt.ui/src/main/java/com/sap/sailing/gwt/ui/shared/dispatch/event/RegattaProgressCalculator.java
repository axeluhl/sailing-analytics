package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class RegattaProgressCalculator implements EventActionUtil.RaceCallback {
    private final Map<String, Set<String>> racesPerSeries = new LinkedHashMap<>();
    private final Map<String, Map<String, Set<String>>> racesPerSeriesAndFleet = new HashMap<>();
    private final Map<String, FleetMetadataDTO> fleets = new HashMap<>();

    @Override
    public void doForRace(RaceContext context) {
        String seriesName = context.getSeriesName();
        
        Set<String> racesInSeries = racesPerSeries.get(seriesName);
        if(racesInSeries == null) {
            racesInSeries = new HashSet<>();
            racesPerSeries.put(seriesName, racesInSeries);
        }
        String raceName = context.getRaceName();
        racesInSeries.add(raceName);
        
        Map<String, Set<String>> racesInSeriesAndFleet = racesPerSeriesAndFleet.get(seriesName);
        if(racesInSeriesAndFleet == null) {
            racesInSeriesAndFleet = new LinkedHashMap<String, Set<String>>();
            racesPerSeriesAndFleet.put(seriesName, racesInSeriesAndFleet);
        }
        String fleetName = context.getFleetName();
        Set<String> racesInFleet = racesInSeriesAndFleet.get(fleetName);
        if(racesInFleet == null) {
            racesInFleet = new HashSet<>();
            racesInSeriesAndFleet.put(fleetName, racesInFleet);
        }
        if(context.isFinished()) {
            racesInFleet.add(raceName);
        }
        
        if(!fleets.containsKey(fleetName)) {
            fleets.put(fleetName, context.getFleetMetadata());
        }
        
    }
    
    public RegattaProgressDTO getResult() {
        RegattaProgressDTO progress = new RegattaProgressDTO();
        for(Map.Entry<String, Set<String>> racesInSeries : racesPerSeries.entrySet()) {
            String seriesName = racesInSeries.getKey();
            int totalRaceCount = racesInSeries.getValue().size();
            RegattaProgressSeriesDTO regattaProgressOfSeries = new RegattaProgressSeriesDTO(seriesName, totalRaceCount);
            for(Map.Entry<String, Set<String>> racesInFleet : racesPerSeriesAndFleet.get(seriesName).entrySet()) {
                String fleetName = racesInFleet.getKey();
                int finishedRacesForFleet = racesInFleet.getValue().size();
                regattaProgressOfSeries.addFleet(fleets.get(fleetName), finishedRacesForFleet);
            }
            progress.addSeries(regattaProgressOfSeries);
        }
        return progress;
    }

}
