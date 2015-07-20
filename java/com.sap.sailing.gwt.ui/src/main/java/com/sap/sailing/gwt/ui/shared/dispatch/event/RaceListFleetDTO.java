package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class RaceListFleetDTO implements IsSerializable {
    
    private FleetMetadataDTO fleet;
    private int competitorCount = (int) (Math.random() * 10); // TODO   
    private ArrayList<RaceListRaceDTO> races = new ArrayList<>();
    
    @SuppressWarnings("unused")
    private RaceListFleetDTO() {
    }
    
    public RaceListFleetDTO(FleetMetadataDTO fleet) {
        this.fleet = fleet;
    }
    
    public void addRace(RaceListRaceDTO race) {
        // TODO insert sorted
        races.add(race);
    }
    
    public FleetMetadataDTO getFleet() {
        return fleet;
    }
    
    public int getCompetitorCount() {
        return competitorCount;
    }
    
    public List<RaceListRaceDTO> getRaces() {
        return races;
    }
}
