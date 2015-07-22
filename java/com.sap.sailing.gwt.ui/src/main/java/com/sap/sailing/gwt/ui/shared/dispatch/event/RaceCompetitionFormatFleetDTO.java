package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;

public class RaceCompetitionFormatFleetDTO implements IsSerializable {
    
    private FleetMetadataDTO fleet;
    private int competitorCount = (int) (Math.random() * 10); // TODO   
    private TreeSet<SimpleRaceMetadataDTO> races = new TreeSet<>();
    
    protected RaceCompetitionFormatFleetDTO() {
    }
    
    public RaceCompetitionFormatFleetDTO(FleetMetadataDTO fleet) {
        this.fleet = fleet;
    }
    
    public void addRace(SimpleRaceMetadataDTO race) {
        races.add(race);
    }
    
    public FleetMetadataDTO getFleet() {
        return fleet;
    }
    
    public int getCompetitorCount() {
        return competitorCount;
    }
    
    public Collection<SimpleRaceMetadataDTO> getRaces() {
        return races;
    }
}
