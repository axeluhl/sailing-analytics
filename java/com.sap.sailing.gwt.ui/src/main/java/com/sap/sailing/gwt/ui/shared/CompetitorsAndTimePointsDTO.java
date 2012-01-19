package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class CompetitorsAndTimePointsDTO implements IsSerializable {
    private static final long MILLISECONDS_BEFORE_RACE_TO_INCLUDE = 20000;
    
    private CompetitorDTO[] competitors;
    private HashMap<String, Pair<String, Long>[]> markPassings;
    private long startTime;
    private long timePointOfNewestEvent;
    private int steps;

    CompetitorsAndTimePointsDTO() {}
    
    public CompetitorsAndTimePointsDTO(int steps) {
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

    public CompetitorDTO[] getCompetitor() {
        return competitors;
    }

    public void setCompetitor(CompetitorDTO[] competitors) {
        this.competitors = competitors;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Pair<String, Long>[] getMarkPassings(CompetitorDTO competitor) {
        return markPassings.get(competitor.id);
    }

    public void setMarkPassings(CompetitorDTO competitor, Pair<String, Long>[] markPassings) {
        this.markPassings.put(competitor.id, markPassings);
    }

    public long getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }

    public void setTimePointOfNewestEvent(long timePointOfNewestEvent) {
        this.timePointOfNewestEvent = timePointOfNewestEvent;
    }

}
