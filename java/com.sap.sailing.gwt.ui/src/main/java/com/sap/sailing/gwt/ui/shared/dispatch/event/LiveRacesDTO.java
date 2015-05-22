package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class LiveRacesDTO implements DTO {
    private ArrayList<LiveRaceDTO> races = new ArrayList<>();
    
    public List<LiveRaceDTO> getRaces() {
        return races;
    }
    
    public void addRace(LiveRaceDTO race) {
        for(int i = 0; i < races.size(); i++) {
            LiveRaceDTO foundRace = races.get(i);
            Date foundStart = foundRace.getStart();
            Date newStart = race.getStart();
            if(foundStart == null || (newStart != null && foundStart.compareTo(newStart) < 0)) {
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
    
    public boolean hasCourses() {
        for (LiveRaceDTO race : races) {
            if (race.getCourse() != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasWind() {
        for (LiveRaceDTO race : races) {
            if (race.getWind() != null) {
                return true;
            }
        }
        return false;
    }
}
