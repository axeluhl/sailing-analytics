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
        for (Mark m : markList) {
            s = s + "|" + m.getIndex() + ";" + m.getDescription() + ";";
            for (String str : m.getDevices()) {
                s = s + str + ";";
            }
            s = s.substring(0, s.length() - 1);
        }
        return "CCG|" +raceId + "|" +  markList.size() + s;
    }

}
