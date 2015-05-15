package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceListDayDTO implements IsSerializable {
    
    private Date day;
    
    private ArrayList<RaceListRaceDTO> races = new ArrayList<>();
    
    @SuppressWarnings("unused")
    private RaceListDayDTO() {
    }
    
    public RaceListDayDTO(Date day) {
        this.day = day;
    }
    
    public void addRace(RaceListRaceDTO race) {
        // TODO insert sorted
        races.add(race);
    }
    
    public Date getDay() {
        return day;
    }
    
    public List<RaceListRaceDTO> getRaces() {
        return races;
    }
}
