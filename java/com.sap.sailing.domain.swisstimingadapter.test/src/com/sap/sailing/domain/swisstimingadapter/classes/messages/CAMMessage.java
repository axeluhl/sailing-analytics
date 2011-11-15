package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Mark;

public class CAMMessage {
    private String raceId;
    private List<Mark> markList;
    public CAMMessage(String raceId, List<Mark> markList) {
        super();
        this.raceId = raceId;
        this.markList = markList;
    }
    public CAMMessage() {
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
   
    
}
