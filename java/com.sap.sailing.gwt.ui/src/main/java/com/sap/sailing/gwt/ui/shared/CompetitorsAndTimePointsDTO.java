package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.SailingService;

/**
 * A data transfer object used as argument to
 * {@link SailingService#getCompetitorRaceData(com.sap.sailing.domain.common.RaceIdentifier, CompetitorsAndTimePointsDTO, com.sap.sailing.domain.common.DetailType)}
 * and as result in
 * {@link SailingService#getCompetitorsAndTimePoints(com.sap.sailing.domain.common.RaceIdentifier, int)}. Objects of this class
 * mainly tell the competitors and the time points for which to load data about a race. The mark passing times are
 * stored in this object because when mark passings are to be displayed in a chart, the point needs an associated value, and
 * hence the function to be represented in the chart needs to be evaluated for the time of the mark passing. This is
 * particularly important if the distance between the other time points is so great that the next time points before and after
 * the mark passing are too far away and therefore the function values may be too different from that at the mark passing time.
 * 
 * @author Benjamin Ebling, Axel Uhl (D043530)
 * 
 */
public class CompetitorsAndTimePointsDTO implements IsSerializable {
    private static final long MILLISECONDS_BEFORE_RACE_TO_INCLUDE = 20000;
    
    private List<CompetitorDTO> competitors;
    private HashMap<String, List<Pair<String, Long>>> markPassings;
    private long startTime;
    private long timePointOfNewestEvent;
    private long stepSize;

    CompetitorsAndTimePointsDTO() {}
    
    public CompetitorsAndTimePointsDTO(long stepSize) {
        markPassings = new HashMap<String, List<Pair<String, Long>>>();
        this.stepSize = stepSize;
    }
    
    public CompetitorsAndTimePointsDTO copy() {
        CompetitorsAndTimePointsDTO clone = new CompetitorsAndTimePointsDTO();
        clone.startTime = startTime;
        clone.timePointOfNewestEvent = timePointOfNewestEvent;
        clone.stepSize = stepSize;
        clone.competitors = new ArrayList<CompetitorDTO>(competitors);
        clone.markPassings = new HashMap<String, List<Pair<String, Long>>>(markPassings);
        return clone;
    }

    public List<Long> getTimePoints() {
        ArrayList<Long> result = new ArrayList<Long>();
        for (long time = startTime - MILLISECONDS_BEFORE_RACE_TO_INCLUDE; time < timePointOfNewestEvent; time += stepSize) {
            result.add(time);
        }
        return result;
    }

    public List<CompetitorDTO> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<CompetitorDTO> competitors) {
        this.competitors = competitors;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public List<Pair<String, Long>> getMarkPassings(CompetitorDTO competitor) {
        return markPassings.get(competitor.id);
    }

    public void setMarkPassings(CompetitorDTO competitor, List<Pair<String, Long>> markPassings) {
        this.markPassings.put(competitor.id, markPassings);
    }

    public long getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }

    public void setTimePointOfNewestEvent(long timePointOfNewestEvent) {
        this.timePointOfNewestEvent = timePointOfNewestEvent;
    }

}
