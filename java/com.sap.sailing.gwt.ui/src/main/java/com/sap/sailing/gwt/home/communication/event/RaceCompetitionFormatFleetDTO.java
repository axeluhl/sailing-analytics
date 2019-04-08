package com.sap.sailing.gwt.home.communication.event;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;

public class RaceCompetitionFormatFleetDTO implements IsSerializable {
    
    private FleetMetadataDTO fleet;
    private int competitorCount = 0;
    private LinkedHashSet<SimpleRaceMetadataDTO> races = new LinkedHashSet<>();
    
    protected RaceCompetitionFormatFleetDTO() {
    }
    
    public RaceCompetitionFormatFleetDTO(FleetMetadataDTO fleet, int competitorCount) {
        this.fleet = fleet;
        this.competitorCount = competitorCount;
    }
    
    public void addRace(SimpleRaceMetadataDTO race) {
        this.races.add(race);
    }
    
    public void addRaces(Collection<SimpleRaceMetadataDTO> races) {
        this.races.addAll(races);
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
