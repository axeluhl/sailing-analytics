package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class RaceListSeriesDTO implements IsSerializable {
    
    private String seriesName;
    private int raceCount = 0;
    private int competitorCount = (int) (Math.random() * 10); // TODO
    private Map<String, RaceListFleetDTO> fleets = new HashMap<>();

    @SuppressWarnings("unused")
    private RaceListSeriesDTO() {
    }
    
    public RaceListSeriesDTO(String seriesName) {
        this.seriesName = seriesName;
    }

    public void addRace(RaceListRaceDTO race) {
        RaceListFleetDTO fleet = ensureFleet(race.getFleet());
        fleet.addRace(race);
        raceCount++;
    }
    
    private RaceListFleetDTO ensureFleet(FleetMetadataDTO fleetData) {
        String fleetName = fleetData == null ? null : fleetData.getFleetName();
        RaceListFleetDTO fleet = fleets.get(fleetName);
        if (fleet == null) {
            fleets.put(fleetName, fleet = new RaceListFleetDTO(fleetData));
        }
        return fleet;
    }
    
    public String getSeriesName() {
        return seriesName;
    }
    
    public int getRaceCount() {
        return raceCount;
    }
    
    public int getCompetitorCount() {
        return competitorCount;
    }
    
    public Collection<RaceListFleetDTO> getFleets() {
        return fleets.values();
    }
    

}
