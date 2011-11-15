package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Mark;

public class CCGMessage {
    private String raceId;
    private List<Mark> markList;
    public CCGMessage(String raceId, List<Mark> markList) {
        super();
        this.raceId = raceId;
        this.markList = markList;
    }
    public CCGMessage() {
        markList = new ArrayList<Mark>();
    }
    public String getRaceId() {
        return raceId;
    }
    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
    public List<Mark> getMarkList() {
        return markList;
    }
    public void setMarkList(List<Mark> markList) {
        this.markList = markList;
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        
        String s = "";
        for (Mark m: markList) {
            s = s + "|" + m.getIndex() + ";" + m.getDescription() + ";";
            for (String str : m.getDevices()) {
                 s = s + str + ";";
            }
        }
        s = s.substring(0, s.length()-2);
        return "CCG|" + markList.size() + s;
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
        CCGMessage other = (CCGMessage) obj;
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
