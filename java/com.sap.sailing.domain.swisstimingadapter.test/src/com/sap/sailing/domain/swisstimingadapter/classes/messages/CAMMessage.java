package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;

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

}
