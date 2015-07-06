package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.TreeSet;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class LiveRacesDTO implements DTO {
    private TreeSet<LiveRaceDTO> races = new TreeSet<>();
    
    public Collection<LiveRaceDTO> getRaces() {
        return races;
    }
    
    public void addRace(LiveRaceDTO race) {
        this.races.add(race);
    }
    
}
