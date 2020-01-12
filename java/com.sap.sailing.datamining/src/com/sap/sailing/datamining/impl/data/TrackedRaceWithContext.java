package com.sap.sailing.datamining.impl.data;

import java.util.Calendar;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
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
        if(getRegatta()!=null) {
            return getRegatta().getDefaultCourseArea();
        }
        return null;
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
        if(getTrackedRace() != null) {
            return getTrackedRace().getRace();
        }
        return null;
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
        if(getTrackedRace() != null) {
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
        return null;
    }
    
    @Override
    public NauticalSide getAdvantageousEndOfLine() {
        if(getTrackedRace() != null) {
            LineDetails startLine = getTrackedRace().getStartLine(getTrackedRace().getStartOfRace());
            return startLine.getAdvantageousSideWhileApproachingLine();
        }
        return null;
    }
    
    @Override
    public Boolean isMedalRace() {
        if(getTrackedRace() != null) {
            return getLeaderboardContext().getLeaderboard().getRaceColumnAndFleet(getTrackedRace()).getA().isMedalRace();
        }
        return null;
    }

    @Override
    public Boolean isTracked() {
        if(getTrackedRace() != null) {
            return getTrackedRace().hasStarted(MillisecondsTimePoint.now());
        }
        return null;
    }
    
    @Override
    public Duration getDuration() {
        if(getTrackedRace() != null) {
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
        return null;
    }

    private <T> int getNumberOfRawFixes(Iterable<T> tracksFor, BiFunction<T, TrackedRace, Track<?>> trackProvider) {
        if(getTrackedRace() != null) {
            int number = 0;
            for (T trackedObject : tracksFor) {
                Track<?> track = trackProvider.apply(trackedObject, getTrackedRace());
                track.lockForRead();
                try {
                    number += Util.size(track.getRawFixes());
                } finally {
                    track.unlockAfterRead();
                }
            }
            return number;
        }
        return 0;
    }
    
    @Override
    public int getNumberOfCompetitorFixes() {
        if(getTrackedRace() != null) {
            return getNumberOfRawFixes(getRace().getCompetitors(), (competitor, trackedRace)->trackedRace.getTrack(competitor));
        }
        return 0;
    }

    @Override
    public int getNumberOfMarkFixes() {
        return getNumberOfRawFixes(getTrackedRace().getMarks(), (mark, trackedRace)->trackedRace.getTrack(mark));
    }
    
    @Override
    public int getNumberOfWindFixes() {
        if(getTrackedRace() != null) {
            final Iterable<WindSource> windSources = getTrackedRace().getWindSources();
            final Iterable<WindSource> windSourcesToExclude = getTrackedRace().getWindSourcesToExclude();
            final Iterable<WindSource> windSourcesToUse = StreamSupport
                    .stream(windSources.spliterator(), /* parallel */ false)
                    .filter(ws -> !Util.contains(windSourcesToExclude, ws))
                    .filter(ws -> ws.getType() != WindSourceType.TRACK_BASED_ESTIMATION
                            && ws.getType() != WindSourceType.MANEUVER_BASED_ESTIMATION)
                    .collect(Collectors.toList());
            return getNumberOfRawFixes(windSourcesToUse, (windSource, trackedRace)->trackedRace.getOrCreateWindTrack(windSource));
        }
        return 0;
    }

    // Convenience methods for race dependent calculation to avoid code duplication
    public Double getRelativeScoreForCompetitor(Competitor competitor) {
        final TimePoint now = MillisecondsTimePoint.now();
        Double maxTotalPoints = 1.0; // avoid division by zero
        Double totalPointsOfCompetitor = null;
        for (final Competitor c : getLeaderboardContext().getLeaderboard().getCompetitors()) {
            final Double totalPoints = getLeaderboardContext().getLeaderboard().getTotalPoints(c, getRaceColumn(), now);
            if (totalPoints != null) {
                maxTotalPoints = Math.max(maxTotalPoints, totalPoints);
                if (c == competitor) {
                    totalPointsOfCompetitor = totalPoints;
                }
            }
        }
        return totalPointsOfCompetitor == null ? null : totalPointsOfCompetitor / maxTotalPoints;
    }
    
    @Override
    public Integer getRankAtFinishForCompetitor(Competitor competitor) {
        if(getTrackedRace() != null) {
            int rank = getTrackedRace().getRank(competitor, getTrackedRace().getEndOfTracking());
            return rank == 0 ? null : rank;
        }
        return null;
    }

    @Override
    public Distance getAdvantageOfStarboardSideOfStartline() {
        if(getTrackedRace() != null) {
            final Distance result;
            if (getTrackedRace().getStartOfRace() != null) {
                final LineDetails startLine = getTrackedRace().getStartLine(getTrackedRace().getStartOfRace());
                if (startLine == null) {
                    result = null;
                } else {
                    final Distance absoluteAdvantage = startLine.getAdvantage();
                    result = startLine.getAdvantageousSideWhileApproachingLine() == NauticalSide.STARBOARD ?
                            absoluteAdvantage : absoluteAdvantage.scale(-1);
                }
            } else {
                result = null;
            }
            return result;
        }
        return null;
    }

    @Override
    public Bearing getTrueWindAngleOfStartLineFromStarboardSide() {
        if(getTrackedRace() != null) {
            final Bearing result;
            if (getTrackedRace().getStartOfRace() != null) {
                final LineDetails startLine = getTrackedRace().getStartLine(getTrackedRace().getStartOfRace());
                if (startLine == null) {
                    result = null;
                } else {
                    final Bearing angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind = startLine.getAngleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind();
                    if (angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind == null) {
                        result = null;
                    } else {
                        result = new DegreeBearingImpl(-180).add(angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind);
                    }
                }
            } else {
                result = null;
            }
            return result;
        }
        return null;
    }

    @Override
    public Distance getStartLineLength() {
        if(getTrackedRace() != null) {
            final Distance result;
            if (getTrackedRace().getStartOfRace() != null) {
                final LineDetails startLine = getTrackedRace().getStartLine(getTrackedRace().getStartOfRace());
                if (startLine == null) {
                    result = null;
                } else {
                    result = startLine.getLength();
                }
            } else {
                result = null;
            }
            return result;
        }
        return null;
    }

    @Override
    public Distance getFinishLineLength() {
        final Distance result;
        if (getTrackedRace().getFinishedTime() != null) {
            final LineDetails finishLine = getTrackedRace().getFinishLine(getTrackedRace().getFinishedTime());
            if (finishLine == null) {
                result = null;
            } else {
                result = finishLine.getLength();
            }
        } else {
            result = null;
        }
        return result;
    }

}