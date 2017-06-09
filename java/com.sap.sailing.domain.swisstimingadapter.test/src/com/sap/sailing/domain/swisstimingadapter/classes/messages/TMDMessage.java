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
        return "TMD|" + raceId + "|" + sailNumber + "|" + list.size() + s;
    }

}
