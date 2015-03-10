package com.sap.sailing.datamining.impl.data;

import java.util.List;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableSpeed;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

public class RaceResultOfCompetitorWithContext implements HasRaceResultOfCompetitorContext {

    private final HasLeaderboardContext leaderboardWithContext;
    private final RaceColumn raceColumn;
    private final Competitor competitor;

    public RaceResultOfCompetitorWithContext(HasLeaderboardContext leaderboardWithContext, RaceColumn raceColumn, Competitor competitor) {
        this.leaderboardWithContext = leaderboardWithContext;
        this.raceColumn = raceColumn;
        this.competitor = competitor;
    }

    @Override
    public HasLeaderboardContext getLeaderboardContext() {
        return leaderboardWithContext;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public double getRelativeRank() {
        Leaderboard leaderboard = getLeaderboardContext().getLeaderboard();
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        double competitorCount = Util.size(leaderboard.getCompetitors());
        double rank = leaderboard.getNetPoints(competitor, raceColumn, now);
        final double result = rank / competitorCount;
        return result;
    }
    
    public Cluster<Speed> getAverageWindSpeedCluster() {
        Speed exactResult = getAverageWindSpeed();
        // TODO continue here...
        return null;
    }
    
    /**
     * If there is no tracked race for the competitor or the race has no wind data, <code>null</code> is returned. Otherwise,
     * the average wind speed for the competitor is sampled in a one-minute interval throughout the race duration.
     */
    private Speed getAverageWindSpeed() {
        final Speed result;
        TrackedRace trackedRace = raceColumn.getTrackedRace(getCompetitor());
        if (trackedRace == null) {
            result = null;
        } else {
            ScalableSpeed windSpeedSum = new ScalableSpeed(Speed.NULL);
            long count = 0;
            final List<Leg> legs = trackedRace.getRace().getCourse().getLegs();
            if (!legs.isEmpty()) {
                result = null;
            } else {
                final Leg firstLeg = legs.get(0);
                final Leg lastLeg = legs.get(legs.size()-1);
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(getCompetitor());
                TimePoint started = trackedRace.getTrackedLeg(getCompetitor(), firstLeg).getStartTime();
                TimePoint finished = trackedRace.getTrackedLeg(getCompetitor(), lastLeg).getFinishTime();
                TimePoint from = started == null ? trackedRace.getStartOfRace() : started;
                TimePoint to = finished == null ? trackedRace.getEndOfRace() : finished;
                for (TimePoint timePoint = from; !timePoint.after(to); timePoint = timePoint.plus(Duration.ONE_MINUTE)) {
                    final Position position;
                    if (track != null) {
                        position = track.getEstimatedPosition(timePoint, /* extrapolate */ false);
                    } else {
                        position = trackedRace.getCenterOfCourse(timePoint);
                    }
                    final Wind wind = trackedRace.getWind(position, timePoint);
                    if (wind != null) {
                        windSpeedSum = windSpeedSum.add(new ScalableSpeed(wind));
                        count++;
                    }
                }
                if (count > 0) {
                    result = windSpeedSum.divide(count);
                } else {
                    result = null;
                }
            }
        }
        return result;
    }
}
