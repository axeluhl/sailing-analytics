package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackedLegOfCompetitorWithContext implements HasTrackedLegOfCompetitorContext {

    private final HasTrackedLegContext trackedLegContext;
    
    private final TrackedLegOfCompetitor trackedLegOfCompetitor;
    private final Competitor competitor;

    private Double rankAtStart;
    private Double rankAtFinish;

    public TrackedLegOfCompetitorWithContext(HasTrackedLegContext trackedLegContext, TrackedLegOfCompetitor trackedLegOfCompetitor) {
        this.trackedLegContext = trackedLegContext;
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.competitor = trackedLegOfCompetitor.getCompetitor();
    }
    
    @Override
    public HasTrackedLegContext getTrackedLegContext() {
        return trackedLegContext;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor() {
        return trackedLegOfCompetitor;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public String getCompetitorSearchTag() {
        return getCompetitor().getSearchTag();
    }
    
    @Override
    public Distance getDistanceTraveled() {
        TimePoint timePoint = getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
        return getTrackedLegOfCompetitor().getDistanceTraveled(timePoint);
    }
    
    @Override
    public Double getRankGainsOrLosses() {
        return getRankAtStart() - getRankAtFinish();
    }
    
    private Double getRankAtStart() {
        if (rankAtStart == null) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            rankAtStart = Double.valueOf(trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getStartTime()));
        }
        return rankAtStart;
    }

    @Override
    public Double getRelativeRank() {
        Leaderboard leaderboard = getTrackedLegContext().getTrackedRaceContext().getLeaderboardContext().getLeaderboard();
        double competitorCount = Util.size(leaderboard.getCompetitors());
        return getRankAtFinish() / competitorCount;
    }

    @Override
    public Double getAbsoluteRank() {
        return getRankAtFinish();
    }
    
    private Double getRankAtFinish() {
        if (rankAtStart == null) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            rankAtFinish = Double.valueOf(trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getFinishTime()));
        }
        return rankAtFinish;
    }
    
}
