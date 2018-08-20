package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sse.common.Util;

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
        String s = "";
        for (Mark m : markList) {
            s = s + "|" + m.getIndex() + ";" + m.getDescription() + ";";
            s += Util.join(";", m.getDeviceIds());
        }
        return "CCG|" +raceId + "|" +  markList.size() + s;
    }
}
