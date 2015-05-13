package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class LiveRacesDTO implements DTO {
    private ArrayList<LiveRaceDTO> races = new ArrayList<>();
    
    public ArrayList<LiveRaceDTO> getRaces() {
        return races;
    }
    
    public void addRace(LiveRaceDTO race) {
        this.races.add(race);
    }
    
    public boolean hasFleets() {
        for(LiveRaceDTO race : races) {
            if(race.getFleet() != null) {
                return true;
            }
        }
        return false;
    }
}
