package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.SailingClusterGroups;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class TrackedLegOfCompetitorWithContext implements HasTrackedLegOfCompetitorContext {
    private static final long serialVersionUID = 5944904146286262768L;

    private final HasTrackedLegContext trackedLegContext;
    
    private final TrackedLegOfCompetitor trackedLegOfCompetitor;
    private final Competitor competitor;

    private Double rankAtStart;
    private boolean isRankAtStartInitialized;
    private Double rankAtFinish;
    private boolean isRankAtFinishInitialized;
    private Wind wind;

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
    public ClusterDTO getPercentageClusterForRelativeScoreInRace() {
        Double relativeScore = getTrackedLegContext().getTrackedRaceContext().getRelativeScoreForCompetitor(getCompetitor());
        if (relativeScore == null) {
            return null;
        }
        
        SailingClusterGroups clusterGroups = Activator.getClusterGroups();
        Cluster<Double> cluster = clusterGroups.getPercentageClusterGroup().getClusterFor(relativeScore);
        return new ClusterDTO(clusterGroups.getPercentageClusterFormatter().format(cluster));
    }
    
    @Override
    public Distance getDistanceTraveled() {
        TimePoint timePoint = getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
        return getTrackedLegOfCompetitor().getDistanceTraveled(timePoint);
    }
    
    @Override
    public Double getRankGainsOrLosses() {
        Double rankAtStart = getRankAtStart();
        Double rankAtFinish = getRankAtFinish();
        return rankAtStart != null && rankAtFinish != null ? rankAtStart - rankAtFinish : null;
    }
    
    private Double getRankAtStart() {
        if (!isRankAtStartInitialized) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            int rank = trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getStartTime());
            rankAtStart = rank == 0 ? null : Double.valueOf(rank);
            isRankAtStartInitialized = true;
        }
        return rankAtStart;
    }

    @Override
    public Double getRelativeRank() {
        Leaderboard leaderboard = getTrackedLegContext().getTrackedRaceContext().getLeaderboardContext().getLeaderboard();
        double competitorCount = Util.size(leaderboard.getCompetitors());
        Double rankAtFinish = getRankAtFinish();
        return rankAtFinish == null ? null : rankAtFinish / competitorCount;
    }

    @Override
    public Double getAbsoluteRank() {
        return getRankAtFinish();
    }
    
    private Double getRankAtFinish() {
        if (!isRankAtFinishInitialized) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            int rank = trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getFinishTime());
            rankAtFinish = rank == 0 ? null : Double.valueOf(rank);
            isRankAtFinishInitialized = true;
        }
        return rankAtFinish;
    }
    
    @Override
    public Long getTimeTakenInSeconds() {
        TimePoint startTime = getTrackedLegOfCompetitor().getStartTime();
        TimePoint finishTime = getTrackedLegOfCompetitor().getFinishTime();
        if (startTime == null || finishTime == null) {
            return null;
        }
        
        return (finishTime.asMillis() - startTime.asMillis()) / 1000;
    }

    @Override
    public Wind getWindInternal() {
        return wind;
    }

    @Override
    public void setWindInternal(Wind wind) {
        this.wind = wind;
    }

    @Override
    public Position getPosition() {
        final TrackedLeg trackedLeg = getTrackedLegContext().getTrackedLeg();
        final TrackedRace trackedRace = trackedLeg.getTrackedRace();
        final TimePoint timepoint = getTimePointBetweenLegStartAndLegFinish(trackedRace);
        final Position result;
        if (timepoint == null) {
            result = null;
        } else {
            result = trackedLeg.getMiddleOfLeg(timepoint);
        }
        return result;
    }

    private TimePoint getTimePointBetweenLegStartAndLegFinish(final TrackedRace trackedRace) {
        final TimePoint competitorLegStartTime = getTrackedLegOfCompetitor().getStartTime();
        final TimePoint competitorLegEndTime =  getTrackedLegOfCompetitor().getFinishTime();
        final TimePoint startTime = competitorLegStartTime != null ? competitorLegStartTime :
            trackedRace.getStartOfRace() != null ? trackedRace.getStartOfRace() : trackedRace.getStartOfTracking();
        final TimePoint endTime = competitorLegEndTime != null ? competitorLegEndTime :
            trackedRace.getEndOfRace() != null ? trackedRace.getEndOfRace() : trackedRace.getEndOfTracking();
        final TimePoint timepoint = endTime == null ? startTime : startTime == null ? null : startTime.plus(startTime.until(endTime).divide(2));
        return timepoint;
    }

    @Override
    public TimePoint getTimePoint() {
        final TrackedLeg trackedLeg = getTrackedLegContext().getTrackedLeg();
        final TrackedRace trackedRace = trackedLeg.getTrackedRace();
        return getTimePointBetweenLegStartAndLegFinish(trackedRace);
    }

    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return this;
    }
    
}
