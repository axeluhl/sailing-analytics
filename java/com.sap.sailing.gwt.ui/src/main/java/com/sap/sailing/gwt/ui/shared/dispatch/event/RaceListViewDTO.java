package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;

public class RaceListViewDTO {
    
    private LiveRacesDTO liveRaces;
    
    private ArrayList<RaceListDayDTO> raceDays = new ArrayList<>();
    
    public void addRace(RaceListRaceDTO race) {
        // TODO implement based on race.getStart();
    }
    
    public LiveRacesDTO getLiveRaces() {
        return liveRaces;
    }
    
    public List<RaceListDayDTO> getRaceDays() {
        return raceDays;
    }
}
