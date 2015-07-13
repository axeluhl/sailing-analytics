package com.sap.sailing.gwt.ui.shared.dispatch.regatta;

import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class RegattaProgressSeriesDTO implements DTO {
    private String name;
    private int totalRaceCount;
    private TreeMap<FleetMetadataDTO, Integer> fleetState = new TreeMap<>();
    
    @SuppressWarnings("unused")
    private RegattaProgressSeriesDTO() {
    }
    
    public RegattaProgressSeriesDTO(String name, int totalRaceCount) {
        this.name = name;
        this.totalRaceCount = totalRaceCount;
    }
    
    public String getName() {
        return name;
    }
    
    public int getTotalRaceCount() {
        return totalRaceCount;
    }
    
    public Map<FleetMetadataDTO, Integer> getFleetState() {
        return fleetState;
    }
    
    public void addFleet(FleetMetadataDTO fleet, int raceCount) {
        fleetState.put(fleet, raceCount);
    }
    
    public boolean isCompleted() {
        for (Integer numberOfRace : fleetState.values()) {
            if (numberOfRace < totalRaceCount) {
                return false;
            }
        }
        return !fleetState.isEmpty();
    }
    
    public int getProgressRaceCount() {
        int progressRaceCount = 0;
        for (Integer numberOfRace : fleetState.values()) {
            progressRaceCount = Math.max(progressRaceCount, numberOfRace);
        }
        return progressRaceCount;
    }
}
