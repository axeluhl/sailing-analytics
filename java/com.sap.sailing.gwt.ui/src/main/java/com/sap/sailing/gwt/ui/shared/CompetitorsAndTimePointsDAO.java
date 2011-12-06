package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorsAndTimePointsDAO implements IsSerializable {
    private CompetitorDAO[] competitors;
    private HashMap<String, Long[]> markPassings;
    private Long[] timePoints;
    private long startTime;
    
    public CompetitorsAndTimePointsDAO(){
        markPassings = new HashMap<String, Long[]>();
    }
    
    public Long[] getTimePoints() {
        return timePoints;
    }
    public void setTimePoints(Long[] timePoints) {
        this.timePoints = timePoints;
    }
    public CompetitorDAO[] getCompetitor() {
        return competitors;
    }
    public void setCompetitor(CompetitorDAO[] competitors) {
        this.competitors = competitors;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public Long[] getMarkPassings(CompetitorDAO competitor) {
        return markPassings.get(competitor.id);
    }
    public void setMarkPassings(CompetitorDAO competitor, Long[] markPassings) {
        this.markPassings.put(competitor.id, markPassings);
    }

    
}
