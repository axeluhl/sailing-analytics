package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorAndTimePointsDAO implements IsSerializable {
    private CompetitorDAO[] competitors;
    private Long[] timePoints;
    private long startTime;
    
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

    
}
