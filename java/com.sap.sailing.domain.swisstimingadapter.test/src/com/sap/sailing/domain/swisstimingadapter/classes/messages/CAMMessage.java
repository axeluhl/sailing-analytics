package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.List;

public class CAMMessage {
    private String raceId;
    private List<ClockAtMarkElement> markList;

    

    public CAMMessage(String raceId, List<ClockAtMarkElement> markList) {
        super();
        this.raceId = raceId;
        this.markList = markList;
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    

    public List<ClockAtMarkElement> getMarkList() {
        return markList;
    }

    public void setMarkList(List<ClockAtMarkElement> markList) {
        this.markList = markList;
    }

    @Override
    public String toString() {
        // TODO data element ändern
        String s = "";
        for (ClockAtMarkElement m : markList) {
            s = s + s.toString();
        }
        return "CAM|" + markList.size() + s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((markList == null) ? 0 : markList.hashCode());
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
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
        CAMMessage other = (CAMMessage) obj;
        if (markList == null) {
            if (other.markList != null)
                return false;
        } else if (!markList.equals(other.markList))
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        return true;
    }
    
    

}
