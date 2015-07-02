package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RaceListViewDTO implements DTO {
    
    private LiveRacesDTO liveRaces;
    
    private ArrayList<RaceListDayDTO> raceDays = new ArrayList<>();
    
    public void addRace(RaceListRaceDTO race) {
        // TODO implement based on race.getStart();
    }
    
    public void setLiveRaces(LiveRacesDTO liveRaces) {
        this.liveRaces = liveRaces;
    }
    
    public LiveRacesDTO getLiveRaces() {
        LiveRacesDTO liveRaces = new LiveRacesDTO();
        for (int i = 0; i < this.liveRaces.getRaces().size(); i++) {
            if (i == 3) break;
            liveRaces.addRace(this.liveRaces.getRaces().get(i));
        }
        return liveRaces ;
    }
    
    public LiveRacesDTO getAllRaces() {
        return liveRaces;
    }
    
    public List<RaceListDayDTO> getRaceDays() {
        return raceDays;
    }
}
