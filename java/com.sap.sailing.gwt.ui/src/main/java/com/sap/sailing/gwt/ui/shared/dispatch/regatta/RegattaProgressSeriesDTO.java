package com.sap.sailing.gwt.ui.shared.dispatch.regatta;

import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class RegattaProgressSeriesDTO implements DTO {
    private String name;
    private int totalRaceCount;
    private TreeMap<FleetMetadataDTO, RegattaProgressFleetDTO> fleetState = new TreeMap<>();
    
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
    
    public Map<FleetMetadataDTO, RegattaProgressFleetDTO> getFleetState() {
        return fleetState;
    }
    
    public void addFleet(FleetMetadataDTO fleet, RegattaProgressFleetDTO stats) {
        fleetState.put(fleet, stats);
    }
    
    public String[] getFleetNames() {
        String[] fleetNames = new String[fleetState.size()];
        int i = 0;
        for (FleetMetadataDTO fleet : fleetState.keySet()) {
            fleetNames[i++] = fleet.getFleetName();
        }
        return fleetNames;
    }
    
    public boolean isCompleted() {
        for (RegattaProgressFleetDTO stats : fleetState.values()) {
            if (stats.getFinishedRaceCount() < totalRaceCount) {
                return false;
            }
        }
        return !fleetState.isEmpty();
    }
    
    public int getProgressRaceCount() {
        int progressRaceCount = 0;
        for (RegattaProgressFleetDTO stats : fleetState.values()) {
            progressRaceCount = Math.max(progressRaceCount, stats.getFinishedRaceCount());
        }
        return progressRaceCount;
    }
}
