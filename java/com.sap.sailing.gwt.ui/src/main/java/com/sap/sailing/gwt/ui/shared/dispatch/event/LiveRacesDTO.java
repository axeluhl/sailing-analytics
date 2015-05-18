package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class LiveRacesDTO implements DTO {
    private ArrayList<LiveRaceDTO> races = new ArrayList<>();
    
    public List<LiveRaceDTO> getRaces() {
        return races;
    }
    
    public void addRace(LiveRaceDTO race) {
        for(int i = 0; i < races.size(); i++) {
            if(races.get(i).getStart().compareTo(race.getStart()) < 0) {
                races.add(i, race);
                return;
            }
        }
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

    public boolean hasCourseAreas() {
        for (LiveRaceDTO race : races) {
            if (race.getCourseArea() != null) {
                return true;
            }
        }
        return false;
    }
}
