package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Competitor;

public class STLMessage {
    private String raceId;
    private List<Competitor> competitorList;

    public STLMessage(String raceId, List<Competitor> competitorList) {
        super();
        this.raceId = raceId;
        this.competitorList = competitorList;
    }

    public STLMessage() {
        competitorList = new ArrayList<Competitor>();
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public List<Competitor> getCompetitorList() {
        return competitorList;
    }

    public void setCompetitorList(List<Competitor> competitorList) {
        this.competitorList = competitorList;
    }

    @Override
    public String toString() {
        String s = "";
        for (Competitor c : competitorList) {
            s = s + "|" + c.getBoatID() + ";" + c.getThreeLetterIOCCode() + ";" + c.getName();
        }
        return "STL|" + raceId + "|" + competitorList.size() + s;
    }
}
