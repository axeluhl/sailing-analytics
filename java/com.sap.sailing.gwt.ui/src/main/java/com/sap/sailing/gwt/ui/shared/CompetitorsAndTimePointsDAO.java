package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorsAndTimePointsDAO implements IsSerializable {
    private static final long MILLISECONDS_BEFORE_RACE_TO_INCLUDE = 20000;
    
    private CompetitorDAO[] competitors;
    private HashMap<String, Pair<String, Long>[]> markPassings;
    private long startTime;
    private long timePointOfNewestEvent;
    private int steps;

    CompetitorsAndTimePointsDAO() {}
    
    public CompetitorsAndTimePointsDAO(int steps) {
        markPassings = new HashMap<String, Pair<String, Long>[]>();
        this.steps = steps;
    }

    public long[] getTimePoints() {
        long[] result = new long[steps];
        int i=0;
        long stepsize = (timePointOfNewestEvent - startTime - MILLISECONDS_BEFORE_RACE_TO_INCLUDE) / steps;
        for (long time = startTime - MILLISECONDS_BEFORE_RACE_TO_INCLUDE; i<result.length && time < timePointOfNewestEvent; time += stepsize) {
            result[i++] = time;
        }
        return result;
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

    public Pair<String, Long>[] getMarkPassings(CompetitorDAO competitor) {
        return markPassings.get(competitor.id);
    }

    public void setMarkPassings(CompetitorDAO competitor, Pair<String, Long>[] markPassings) {
        this.markPassings.put(competitor.id, markPassings);
    }

    public long getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }

    public void setTimePointOfNewestEvent(long timePointOfNewestEvent) {
        this.timePointOfNewestEvent = timePointOfNewestEvent;
    }

}
