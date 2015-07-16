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
