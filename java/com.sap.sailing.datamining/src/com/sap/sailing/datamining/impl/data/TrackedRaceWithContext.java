package com.sap.sailing.datamining.impl.data;

import java.util.Calendar;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedRaceWithContext implements HasTrackedRaceContext {

    private final HasLeaderboardContext leaderboardContext;
    private final Regatta regatta;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final TrackedRace trackedRace;
    
    private Integer year;
    private boolean yearHasBeenInitialized;

    public TrackedRaceWithContext(HasLeaderboardContext leaderboardContext, Regatta regatta, RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        this.leaderboardContext = leaderboardContext;
        this.regatta = regatta;
        this.raceColumn = raceColumn;
        this.fleet = fleet;
        this.trackedRace = trackedRace;
    }
    
    @Override
    public HasLeaderboardContext getLeaderboardContext() {
        return leaderboardContext;
    }
    
    @Override
    public Regatta getRegatta() {
        return regatta;
    }
    
    @Override
    public CourseArea getCourseArea() {
        return getRegatta().getDefaultCourseArea();
    }
    
    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }
    
    @Override
    public RaceDefinition getRace() {
        return getTrackedRace().getRace();
    }

    @Override
    public Integer getYear() {
        if (!yearHasBeenInitialized) {
            year = calculateYear();
            yearHasBeenInitialized = true;
        }
        return year;
    }

    private Integer calculateYear() {
        TimePoint startOfRace = getTrackedRace().getStartOfRace();
        TimePoint time = startOfRace != null ? startOfRace : getTrackedRace().getStartOfTracking();
        final Integer result;
        if (time == null) {
            result = 0;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time.asDate());
            result = calendar.get(Calendar.YEAR);
        }
        return result;
    }
    
    @Override
    public NauticalSide getAdvantageousEndOfLine() {
        LineDetails startLine = getTrackedRace().getStartLine(getTrackedRace().getStartOfRace());
        return startLine.getAdvantageousSideWhileApproachingLine();
    }
    
    @Override
    public Boolean isMedalRace() {
        return getLeaderboardContext().getLeaderboard().getRaceColumnAndFleet(getTrackedRace()).getA().isMedalRace();
    }

    @Override
    public Boolean isTracked() {
        return getTrackedRace().hasStarted(MillisecondsTimePoint.now());
    }
    
    @Override
    public Duration getDuration() {
        Duration duration = null;
        TrackedRace race = getTrackedRace();
        TimePoint start = race.getStartOfRace();
        if (start == null) {
            start = race.getStartOfTracking();
        }
        if (start != null) {
            TimePoint end = race.getEndOfRace();
            if (end == null) {
                end = race.getEndOfTracking();
            }
            if (end != null) {
                duration = new MillisecondsDurationImpl(end.asMillis() - start.asMillis());
            }
        }
        return duration;
    }

    @Override
    public int getNumberOfCompetitorFixes() {
        int number = 0;
        for (Competitor competitor : getRace().getCompetitors()) {
            GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(competitor);
            track.lockForRead();
            try {
                number += Util.size(track.getRawFixes());
            } finally {
                track.unlockAfterRead();
            }
        }
        return number;
    }

    @Override
    public int getNumberOfMarkFixes() {
        int number = 0;
        for (Mark mark : getTrackedRace().getMarks()) {
            GPSFixTrack<Mark, GPSFix> track = getTrackedRace().getTrack(mark);
            track.lockForRead();
            try {
                number += Util.size(track.getRawFixes());
            } finally {
                track.unlockAfterRead();
            }
        }
        return number;
    }
    
    // Convenience methods for race dependent calculation to avoid code duplication
    public Double getRelativeScoreForCompetitor(Competitor competitor) {
        final TimePoint now = MillisecondsTimePoint.now();
        Double maxTotalPoints = 1.0; // avoid division by zero
        Double totalPointsOfCompetitor = 0.0;
        for (final Competitor c : getLeaderboardContext().getLeaderboard().getCompetitors()) {
            final Double totalPoints = getLeaderboardContext().getLeaderboard().getTotalPoints(c, getRaceColumn(), now);
            maxTotalPoints = Math.max(maxTotalPoints, totalPoints);
            if (c == competitor) {
                totalPointsOfCompetitor = totalPoints;
            }
        }
        return totalPointsOfCompetitor / maxTotalPoints;
    }
    
    @Override
    public Double getRankAtFinishForCompetitor(Competitor competitor) {
        int rank = getTrackedRace().getRank(competitor, getTrackedRace().getEndOfTracking());
        return rank == 0 ? null : Double.valueOf(rank);
    }

}