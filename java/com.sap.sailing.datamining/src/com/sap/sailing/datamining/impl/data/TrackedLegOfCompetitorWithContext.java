package com.sap.sailing.datamining.impl.data;

import java.util.List;
import java.util.function.BiFunction;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.SailingClusterGroups;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class TrackedLegOfCompetitorWithContext implements HasTrackedLegOfCompetitorContext {
    private static final long serialVersionUID = 5944904146286262768L;

    private final HasTrackedLegContext trackedLegContext;
    
    private final TrackedLegOfCompetitor trackedLegOfCompetitor;
    private final Competitor competitor;

    private Integer rankAtStart;
    private boolean isRankAtStartInitialized;
    private Integer rankAtFinish;
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
    public String getSailID() {
        Boat boatOfCompetitor = getTrackedRace().getBoatOfCompetitor(getCompetitor());
        return boatOfCompetitor != null ? boatOfCompetitor.getSailID() : null;
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
    
    protected <R> R getSomethingForLegTrackingInterval(BiFunction<TimePoint, TimePoint, R> resultSupplier) {
        final TimePoint startTime = getTrackedLegOfCompetitor().getStartTime();
        final TimePoint finishTime = getTrackedLegOfCompetitor().getFinishTime();
        return getSomethingForInterval(resultSupplier, startTime, finishTime);
    }

    protected <R> R getSomethingForInterval(BiFunction<TimePoint, TimePoint, R> resultSupplier,
            final TimePoint startTime, final TimePoint finishTime) {
        final R result;
        if (startTime != null) {
            final TrackedRace trackedRace = getTrackedLegContext().getTrackedLeg().getTrackedRace();
            final TimePoint effectiveEndOfInterval = finishTime != null ? finishTime :
                trackedRace.getEndOfTracking() != null ? trackedRace.getEndOfTracking() : MillisecondsTimePoint.now();
            result = resultSupplier.apply(startTime, effectiveEndOfInterval);
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public Duration getTimeSpentFoiling() {
        return getSomethingForLegTrackingInterval((start, end) -> {
            BravoFixTrack<Competitor> bravoFixTrack = getTrackedLegContext().getTrackedLeg().getTrackedRace().getSensorTrack(getCompetitor(), BravoFixTrack.TRACK_NAME);
            return bravoFixTrack == null ? Duration.NULL : bravoFixTrack.getTimeSpentFoiling(start, end);
        });
    }

    @Override
    public Distance getDistanceSpentFoiling() {
        return getSomethingForLegTrackingInterval((start, end) -> {
            BravoFixTrack<Competitor> bravoFixTrack = getTrackedLegContext().getTrackedLeg().getTrackedRace().getSensorTrack(getCompetitor(), BravoFixTrack.TRACK_NAME);
            return bravoFixTrack == null ? Distance.NULL : bravoFixTrack.getDistanceSpentFoiling(start, end);
        });
    }

    @Override
    public Distance getDistanceTraveled() {
        TimePoint timePoint = getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
        return getTrackedLegOfCompetitor().getDistanceTraveled(timePoint);
    }
    
    @Override
    public Double getSpeedAverage() {
        if (getTrackedRace() == null) {
            return null;
        } else {
            final List<Leg> legs = getTrackedRace().getRace().getCourse().getLegs();
            if (legs.isEmpty()) {
                return null;
            } else {
                final Leg lastLeg = legs.get(legs.size() - 1);
                TimePoint finished = getTrackedRace().getTrackedLeg(getCompetitor(), lastLeg).getFinishTime();
                return getTrackedRace().getAverageSpeedOverGround(getCompetitor(), finished).getKnots();
            }
        }
    }
    
    @Override 
    public Pair<Double, Double> getSpeedAverageVsDistanceTraveled(){
        return new Pair<>(getSpeedAverage(), getDistanceTraveled().getMeters());
    }
    
    @Override
    public Integer getRankGainsOrLosses() {
        Integer rankAtStart = getRankAtStart();
        Integer rankAtFinish = getRankAtFinish();
        return rankAtStart != null && rankAtFinish != null ? rankAtStart - rankAtFinish : null;
    }
    
    private Integer getRankAtStart() {
        if (!isRankAtStartInitialized) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            int rank = trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getStartTime());
            rankAtStart = rank == 0 ? null : rank;
            isRankAtStartInitialized = true;
        }
        return rankAtStart;
    }

    @Override
    public Double getRelativeRank() {
        Leaderboard leaderboard = getTrackedLegContext().getTrackedRaceContext().getLeaderboardContext().getLeaderboard();
        double competitorCount = Util.size(leaderboard.getCompetitors());
        Integer rankAtFinish = getRankAtFinish();
        return rankAtFinish == null ? null : rankAtFinish / competitorCount;
    }
    
    public Integer getRankAtFinish() {
        if (!isRankAtFinishInitialized) {
            TrackedRace trackedRace = getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            int rank = trackedRace.getRank(getCompetitor(), getTrackedLegOfCompetitor().getFinishTime());
            rankAtFinish = rank == 0 ? null : rank;
            isRankAtFinishInitialized = true;
        }
        return rankAtFinish;
    }
    
    @Override
    public Long getTimeTakenInSeconds() {
        TimePoint startTime = getTrackedLegOfCompetitor().getStartTime();
        TimePoint finishTime = getTrackedLegOfCompetitor().getFinishTime();
        final Long result;
        if (startTime == null || finishTime == null) {
            result = null;
        } else {
            result = (finishTime.asMillis() - startTime.asMillis()) / 1000;
        }
        return result;
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

    /**
     * Picks the time point that is in the middle between the time point when the competitor entered
     * the leg and the time point the competitor finished the leg. If no leg start/finish time exists
     * for the competitor, start/end of race and then start/end of tracking are used as fall-back values.
     */
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
    
    @Override
    public Integer getNumberOfManeuvers() {
        return getNumberOfJibes() + getNumberOfTacks();
    }

    @Override
    public Integer getNumberOfJibes() {
        return getSomethingForLegTrackingInterval((start, end) -> {
            return getNumberOf(ManeuverType.JIBE, start, end);
        });
    }

    @Override
    public Integer getNumberOfTacks() {
        return getSomethingForLegTrackingInterval((start, end) -> {
            return getNumberOf(ManeuverType.TACK, start, end);
        });
    }
    
    private int getNumberOf(ManeuverType maneuverType, TimePoint start, TimePoint end) {
        TrackedRace trackedRace = getTrackedRace();
        int number = 0;

        if (trackedRace != null)
            for (Maneuver maneuver : trackedRace.getManeuvers(getCompetitor(), start, end, false)) {
                if (maneuver.getType() == maneuverType) {
                    number++;
                }
            }

        return number;
    }
}
