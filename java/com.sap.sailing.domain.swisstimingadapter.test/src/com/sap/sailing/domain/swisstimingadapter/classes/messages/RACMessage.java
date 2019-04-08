package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Race;

public class RACMessage {
    private List<Race> raceList;

    public RACMessage(List<Race> raceList) {
        super();
        this.raceList = raceList;
    }

    public RACMessage() {
        raceList = new ArrayList<Race>();
    }

    public List<Race> getRaceList() {
        return raceList;
    }

    public void setRaceList(List<Race> raceList) {
        this.raceList = raceList;
    }
    
    @Override
    public String toString() {
        String s = "";
        for (Race race : raceList) {
            s = s + "|" + race.getRaceID() + ";" + race.getDescription();
        }
        return "RAC|" + raceList.size() + s;
    }

    
}
