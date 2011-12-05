package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorAndTimePointsDAO implements IsSerializable {
    private List<Pair<CompetitorDAO, Long[]>> competitorWithMarkPassing;
    private Long[] timePoints;
    private long startTime;
    
    public CompetitorAndTimePointsDAO(){
        competitorWithMarkPassing = new ArrayList<Pair<CompetitorDAO,Long[]>>();
    }
    
    public Long[] getTimePoints() {
        return timePoints;
    }
    public void setTimePoints(Long[] timePoints) {
        this.timePoints = timePoints;
    }
    public CompetitorDAO[] getCompetitor() {
        CompetitorDAO[] competitors = new CompetitorDAO[competitorWithMarkPassing.size()];
        for (int i  = 0; i < competitors.length; i++){
            competitors[i] = competitorWithMarkPassing.get(i).getA();
        }
        return competitors;
    }
    public Long[] getMarkPassings(CompetitorDAO competitor){
        for (int i = 0; i < competitorWithMarkPassing.size(); i++) {
            if (competitorWithMarkPassing.get(i).getA().id == competitor.id){
                return competitorWithMarkPassing.get(i).getB();
            }
        }
        return null;
    }
    public void addCompetitorAndMarkPassing(CompetitorDAO competitor, Long[] markPassings) {
        competitorWithMarkPassing.add(new Pair<CompetitorDAO, Long[]>(competitor, markPassings));
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    
}
