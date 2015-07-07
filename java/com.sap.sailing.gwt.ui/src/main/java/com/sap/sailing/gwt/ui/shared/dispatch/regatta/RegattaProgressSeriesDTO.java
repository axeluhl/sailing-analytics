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
}
