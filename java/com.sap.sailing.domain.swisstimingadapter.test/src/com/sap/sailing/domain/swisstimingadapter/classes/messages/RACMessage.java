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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((raceList == null) ? 0 : raceList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RACMessage other = (RACMessage) obj;
        if (raceList == null) {
            if (other.raceList != null)
                return false;
        } else if (!raceList.equals(other.raceList))
            return false;
        return true;
    }
    
    
}
