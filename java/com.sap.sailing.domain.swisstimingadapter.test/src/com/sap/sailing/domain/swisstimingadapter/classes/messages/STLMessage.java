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
        for(Competitor c : competitorList){
            s = s + "|" + c.getBoatID() + ";" + c.getThreeLetterIOCCode() + ";" + c.getName();
        }
        return "STL|" + competitorList.size() + s;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitorList == null) ? 0 : competitorList.hashCode());
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
        STLMessage other = (STLMessage) obj;
        if (competitorList == null) {
            if (other.competitorList != null)
                return false;
        } else if (!competitorList.equals(other.competitorList))
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        return true;
    }
    
}
