package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

public class TMDMessage {
    private String raceId;
    private String sailNumber;
    private List<TimingDataElement> list;

    public TMDMessage(String raceId, String sailNumber, List<TimingDataElement> list) {
        super();
        this.raceId = raceId;
        this.sailNumber = sailNumber;
        this.list = list;
    }

    public TMDMessage() {
        list = new ArrayList<TimingDataElement>();
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public String getSailNumber() {
        return sailNumber;
    }

    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }

    public List<TimingDataElement> getList() {
        return list;
    }

    public void setList(List<TimingDataElement> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        String s = "";
        for (TimingDataElement el : list) {
            s = s + el.toString();
        }

        return "TMD|" + list.size() + s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((list == null) ? 0 : list.hashCode());
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((sailNumber == null) ? 0 : sailNumber.hashCode());
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
        TMDMessage other = (TMDMessage) obj;
        if (list == null) {
            if (other.list != null)
                return false;
        } else if (!list.equals(other.list))
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        if (sailNumber == null) {
            if (other.sailNumber != null)
                return false;
        } else if (!sailNumber.equals(other.sailNumber))
            return false;
        return true;
    }
    
    

}
