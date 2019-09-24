package com.sap.sailing.domain.orc.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;

import com.sap.sailing.domain.abstractlog.orc.ORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentFinder;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataAnalyzer;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCScratchBoatEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCScratchBoatFinder;
import com.sap.sailing.domain.abstractlog.orc.RegattaLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.orc.RegattaLogORCCertificateAssignmentFinder;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCLegDataEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.ranking.AbstractRankingMetric;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ORCPerformanceCurveByImpliedWindRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -7814822523533929816L;
    private static final Logger logger = Logger.getLogger(ORCPerformanceCurveByImpliedWindRankingMetric.class.getName());
    
    /**
     * This field contains a map of all current certificates used for calculation in this {@link TrackedRace}. Each
     * participating {@link Competitor} with one {@link Boat} has only one currently active {@link ORCCertificate}.
     * The map is initialized and updated by {@link #updateCertificatesFromLogs()} which is invoked each time a
     * new certificate mapping is announced in any of the logs or if a new log is attached or an existing log
     * is detached.
     */
    private Map<Boat, ORCCertificate> certificates;
    
    private ORCPerformanceCurveCourse totalCourse;
    
    private final Map<Serializable, Boat> boatsById;
    
    private final Map<Serializable, Competitor> competitorsById;
    
    private final RaceLogEventVisitor certificatesFromRaceLogUpdater;
    
    private final RegattaLogEventVisitor certificatesFromRegattaLogUpdater;
    
    /**
     * Updated by an observer pattern that watches all {@link RaceLog}s {@link TrackedRace#getAttachedRaceLogs() attached} to the
     * {@link #getTrackedRace() tracked race} for occurrence and revocations of {@link RaceLogORCScratchBoatEvent}s.
     */
    private Competitor explicitScratchBoat;
    
    /**
     * TODO maybe it's a good idea to cache the {@link ORCPerformanceCurve} objects and implied wind speeds for all competitors involved in this object which serves as some sort of cache for ranking calculations for a single time point
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class ORCPerformanceCurveRankingInfo extends AbstractRankingInfoWithCompetitorRankingInfoCache {
        private static final long serialVersionUID = -3578498778702139675L;
        
        /**
         * Uses the {@link ORCPerformanceCurveRankingMetric#getScratchBoat(TimePoint) scratch boat} as the "boat
         * farthest ahead." The default scratch boat is defined as such but may be overridden explicitly.
         */
        public ORCPerformanceCurveRankingInfo(TimePoint timePoint, Map<Competitor, CompetitorRankingInfo> competitorRankingInfo, WindLegTypeAndLegBearingCache cache) {
            super(timePoint, competitorRankingInfo, getScratchBoat(timePoint, cache));
        }
    }
    
    public ORCPerformanceCurveByImpliedWindRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
        boatsById = initBoatsById();
        competitorsById = initCompetitorsById();
        updateCertificatesFromLogs();
        updateCourseFromRaceLogs();
        certificatesFromRaceLogUpdater = createCertificatesFromRaceLogAndCourseUpdater();
        certificatesFromRegattaLogUpdater = createCertificatesFromRegattaLogUpdater();
        if (trackedRace != null) {
            trackedRace.addListener(new AbstractRaceChangeListener() {
                @Override
                public void regattaLogAttached(RegattaLog regattaLog) {
                    regattaLog.addListener(certificatesFromRegattaLogUpdater);
                    updateCertificatesFromLogs();
                }
    
                @Override
                public void raceLogAttached(RaceLog raceLog) {
                    raceLog.addListener(certificatesFromRaceLogUpdater);
                    updateCertificatesFromLogs();
                    updateScratchBoatFromLogs();
                }
    
                @Override
                public void raceLogDetached(RaceLog raceLog) {
                    raceLog.removeListener(certificatesFromRaceLogUpdater);
                    updateCertificatesFromLogs();
                    updateScratchBoatFromLogs();
                }
            });
            trackedRace.getRace().getCourse().addCourseListener(new CourseListener() {
                @Override
                public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
                    updateCourseFromRaceLogs();
                }
                
                @Override
                public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
                    updateCourseFromRaceLogs();
                }
            });
        }
    }
    
    public ORCCertificate getCertificate(Boat boat) {
        return certificates.get(boat);
    }

    private Map<Serializable, Boat> initBoatsById() {
        final Map<Serializable, Boat> result = new HashMap<>();
        if (getTrackedRace() != null) {
            for (final Boat boat : getTrackedRace().getTrackedRegatta().getRegatta().getAllBoats()) {
                result.put(boat.getId(), boat);
            }
        }
        return result;
    }

    private Map<Serializable, Competitor> initCompetitorsById() {
        final Map<Serializable, Competitor> result = new HashMap<>();
        if (getTrackedRace() != null) {
            for (final Competitor competitor : getTrackedRace().getTrackedRegatta().getRegatta().getAllCompetitors()) {
                result.put(competitor.getId(), competitor);
            }
        }
        return result;
    }

    private RaceLogEventVisitor createCertificatesFromRaceLogAndCourseUpdater() {
        return new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogORCLegDataEvent orcLegDataEventImpl) {
                updateCourseFromRaceLogs();
            }

            @Override
            public void visit(RaceLogORCCertificateAssignmentEvent event) {
                updateCertificatesFromLogs();
            }

            @Override
            public void visit(RaceLogRevokeEvent event) {
                if (event.getRevokedEventType().equals(RaceLogORCLegDataEventImpl.class.getName())) {
                    updateCourseFromRaceLogs();
                } else if (event.getRevokedEventType().equals(RaceLogORCLegDataEventImpl.class.getName())) {
                    updateScratchBoatFromLogs();
                }
            }

            @Override
            public void visit(RaceLogORCScratchBoatEvent event) {
                updateScratchBoatFromLogs();
            }
        };
    }

    private RegattaLogEventVisitor createCertificatesFromRegattaLogUpdater() {
        return new BaseRegattaLogEventVisitor() {
            @Override
            public void visit(RegattaLogORCCertificateAssignmentEvent event) {
                updateCertificatesFromLogs();
            }
        };
    }

    /**
     * For now, we assume that there is only one scratch boat defined for this race and that it's independent of the
     * pass. We pick the last ("greatest") valid event of type {@link RaceLogORCScratchBoatEvent}.
     */
    private void updateScratchBoatFromLogs() {
        Competitor scratchBoatFromLog = null;
        for (final RaceLog raceLog : getTrackedRace().getAttachedRaceLogs()) {
            scratchBoatFromLog = new RaceLogORCScratchBoatFinder(raceLog, competitorsById).analyze();
            if (scratchBoatFromLog != null) {
                break;
            }
        }
        explicitScratchBoat = scratchBoatFromLog;
    }

    /**
     * To be called if the set of logs attached to the {@link #getTrackedRace() tracked race} changes or any of those
     * logs sees a {@link ORCCertificateAssignmentEvent} being added. As a result, the {@link #certificates} map is
     * replaced by a new one that has the updated mapping of boats to their certificates.
     */
    private void updateCertificatesFromLogs() {
        final Map<Boat, ORCCertificate> newCertificates = new HashMap<>();
        if (getTrackedRace() != null) {
            for (final RegattaLog regattaLog : getTrackedRace().getAttachedRegattaLogs()) {
                newCertificates.putAll(new RegattaLogORCCertificateAssignmentFinder(regattaLog, boatsById).analyze());
            }
            for (final RaceLog raceLog : getTrackedRace().getAttachedRaceLogs()) {
                newCertificates.putAll(new RaceLogORCCertificateAssignmentFinder(raceLog, boatsById).analyze());
            }
        }
        certificates = newCertificates;
    }
    
    private void updateCourseFromRaceLogs() {
        final Map<Integer, ORCPerformanceCurveLeg> legsWithDefinitions = new HashMap<>();
        if (getTrackedRace() != null) {
            for (final RaceLog raceLog : getTrackedRace().getAttachedRaceLogs()) {
                legsWithDefinitions.putAll(new RaceLogORCLegDataAnalyzer(raceLog).analyze());
            }
            final Iterable<Leg> legs = getTrackedRace().getRace().getCourse().getLegs();
            int oneBasedLegNumber = 1;
            final List<ORCPerformanceCurveLeg> performanceCurveLegs = new ArrayList<>(Util.size(legs));
            for (final Leg leg : legs) {
                performanceCurveLegs.add(legsWithDefinitions.computeIfAbsent(oneBasedLegNumber, i->new ORCPerformanceCurveLegAdapter(getTrackedRace().getTrackedLeg(leg))));
                oneBasedLegNumber++;
            }
            totalCourse = new ORCPerformanceCurveCourseImpl(performanceCurveLegs);
        }
    }
    
    public ORCPerformanceCurveCourse getTotalCourse() {
        return totalCourse;
    }

    private ORCPerformanceCurveCourse getPartialCourse(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final ORCPerformanceCurveCourse result;
        final Leg firstLeg = getTrackedRace().getRace().getCourse().getFirstLeg();
        final TrackedLegOfCompetitor trackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, timePoint);
        if (trackedLegOfCompetitor == null) {
            if (getTrackedRace().getTrackedLeg(competitor, firstLeg).hasStartedLeg(timePoint)) {
                // then we know the competitor has finished the race at timePoint
                result = getTotalCourse();
            } else {
                // not started the race yet; return empty course
                result = getTotalCourse().subcourse(0, 0);
            }
        } else {
            final double shareOfCurrentLeg = 1.0
                    - trackedLegOfCompetitor.getWindwardDistanceToGo(timePoint, WindPositionMode.LEG_MIDDLE, cache).divide(
                            trackedLegOfCompetitor.getTrackedLeg().getWindwardDistance(timePoint, cache));
            result = getTotalCourse().subcourse(getTrackedRace().getRace().getCourse().getIndexOfWaypoint(trackedLegOfCompetitor.getLeg().getFrom()), shareOfCurrentLeg);
        }
        return result;
    }
    
    /**
     * A "scratch boat" in the logic of ORC Performance Curve Scoring is used to map the official ranking criterion
     * (implied wind) to a metric that is easier to grasp: calculated time. The scratch boat's
     * {@link ORCPerformanceCurve performance curve} is used to map everybody else's implied wind to a duration spent
     * sailing that can be compared to the duration sailed by the scratch boat.
     * <p>
     * 
     * By default, we use the boat farthest ahead ("first ship home" when the race has finished) as the scratch boat,
     * but other definitions are possible, such as greatest {@link ORCCertificate#getGPH() GPH value} or least
     * {@link ORCCertificate#getCDL() CDL value}. An explicit selection can be made using the
     * {@link RaceLogORCScratchBoatEvent} in the {@link RaceLog}. The last unrevoked event in the first {@link RaceLog}
     * in which one is found is used. Only if none of the attached {@link RaceLog}s contains any unrevoked such event,
     * the scratch boat is selected implicitly to be the boat farthest ahead (see
     * {@link #getCompetitorFarthestAhead(TimePoint, WindLegTypeAndLegBearingCache)}).
     */
    private Competitor getScratchBoat(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Competitor result;
        if (explicitScratchBoat != null) {
            result = explicitScratchBoat;
        } else {
            result = getCompetitorFarthestAhead(timePoint, cache);
        }
        return result;
    }
    
    /**
     * Implementation approach: compute the implied wind values for all competitors and base a comparator implementation
     * on those values.
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = getImpliedWindByCompetitor(timePoint, cache);
        return (c1, c2)->Comparator.nullsLast((Speed impliedWindSpeed1, Speed impliedWindSpeed2)->impliedWindSpeed2.compareTo(impliedWindSpeed1)).
                compare(impliedWindByCompetitor.get(c1), impliedWindByCompetitor.get(c2));
    }

    private Map<Competitor, Speed> getImpliedWindByCompetitor(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            try {
                impliedWindByCompetitor.put(competitor, getImpliedWind(competitor, timePoint, cache));
            } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                // log and leave entry for competitor empty; this, together with a nullsLast comparator
                // will sort such competitors towards "worse" ranks
                logger.log(Level.WARNING, "Problem trying to determine ORC PCS implied wind for competitor "
                        + competitor + " for time point " + timePoint, e);
            }
        }
        return impliedWindByCompetitor;
    }
    
    protected ORCPerformanceCurve getPerformanceCurveForPartialCourse(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) throws FunctionEvaluationException {
        final ORCPerformanceCurveCourse competitorsPartialCourseAtTimePoint = getPartialCourse(competitor, timePoint, cache);
        final ORCPerformanceCurve performanceCurveForPartialCourse;
        if (competitorsPartialCourseAtTimePoint.getTotalLength().equals(Distance.NULL)) {
            // can't compute a performance curve for an empty course
            performanceCurveForPartialCourse = null;
        } else {
            final ORCCertificate certificate = getCertificate(getTrackedRace().getBoatOfCompetitor(competitor));
            if (certificate != null) {
                performanceCurveForPartialCourse = new ORCPerformanceCurveImpl(certificate, competitorsPartialCourseAtTimePoint);
            } else {
                performanceCurveForPartialCourse = null;
            }
        }
        return performanceCurveForPartialCourse;
    }

    protected Speed getImpliedWind(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) throws FunctionEvaluationException, MaxIterationsExceededException {
        final Speed result;
        final ORCPerformanceCurve performanceCurveForPartialCourse = getPerformanceCurveForPartialCourse(competitor, timePoint, cache);
        if (performanceCurveForPartialCourse != null) {
            final Duration timeSailedSinceRaceStart = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
            if (timeSailedSinceRaceStart != null) {
                result = performanceCurveForPartialCourse.getImpliedWind(timeSailedSinceRaceStart);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Determines the ranks in the leg identified by {@code trackedLeg}. The outcome depends on whether the competitors
     * have started / finished the leg at {@code timePoint}. The following combinations have to be distinguished:
     * <ol>
     * <li>Both haven't started the leg yet at {@code timePoint}: both compare equal</li>
     * <li>One has, one hasn't started the let yet at {@code timePoint}: the one that has started compares "better"
     * (less)</li>
     * <li>Both have started the leg at {@code timePoint}: their implied wind speeds at {@code timePoint} or the point
     * in time when the respective competitor finished the leg---whichever is earlier---are compared. More means better
     * (less in terms of the comparator returned).</li>
     * </ol>
     */
    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            final TimePoint timePointForImpliedWind;
            if (trackedLeg.getTrackedLeg(competitor).hasFinishedLeg(timePoint)) {
                timePointForImpliedWind = trackedLeg.getTrackedLeg(competitor).getFinishTime();
            } else {
                timePointForImpliedWind = timePoint;
            }
            try {
                impliedWindByCompetitor.put(competitor, getImpliedWind(competitor, timePointForImpliedWind, cache));
            } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                // log and leave entry for competitor empty; this, together with a nullsLast comparator
                // will sort such competitors towards "worse" ranks
                logger.log(Level.WARNING, "Problem trying to determine ORC PCS implied wind for competitor "
                        + competitor + " for time point " + timePoint, e);
            }
        }
        return (tloc1, tloc2)->{
            final int result;
            final boolean hasStarted1 = tloc1.hasStartedLeg(timePoint);
            final boolean hasStarted2 = tloc2.hasStartedLeg(timePoint);
            if (!hasStarted1) {
                if (!hasStarted2) {
                    // both haven't started; they are considered equal for the leg under consideration
                    result = 0;
                } else {
                    // competitor 1 has not started the leg yet, competitor 2 has started the leg, so competitor
                    // 1 is worse (greater) than 2
                    result = 1;
                }
            } else {
                if (!hasStarted2) {
                    // competitor 1 has started the leg, competitor 2 hasn't, so competitor 1 is better (less)
                    result = -1;
                } else {
                    // both have started; use timePoint or the respective leg finishing time, whichever comes first,
                    // and determine the implied wind
                    result = Comparator
                            .nullsLast((Speed impliedWind1, Speed impliedWind2) -> impliedWind2.compareTo(impliedWind1))
                            .compare(impliedWindByCompetitor.get(tloc1.getCompetitor()),
                                    impliedWindByCompetitor.get(tloc2.getCompetitor()));
                }
            }
            return result;
        };
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Duration result = null;
        final Competitor scratchBoat = getScratchBoat(timePoint, cache);
        if (competitor == scratchBoat) {
            // the scratch boat's corrected time is its own time sailed
            result = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
        } else {
            ORCPerformanceCurve scratchBoatPerformanceCurve;
            // these try-catch clauses seem a bit clumsy, but we'd like to log differently, depending on
            // where the FunctionEvaluationException is thrown
            try {
                scratchBoatPerformanceCurve = getPerformanceCurveForPartialCourse(scratchBoat, timePoint, cache);
            } catch (FunctionEvaluationException e) {
                logger.log(Level.WARNING, "Problem evaluating performance curve function for scratch boat "+scratchBoat, e);
                scratchBoatPerformanceCurve = null;
            }
            if (scratchBoatPerformanceCurve != null) {
                ORCPerformanceCurve competitorPerformanceCurve;
                try {
                    competitorPerformanceCurve = getPerformanceCurveForPartialCourse(competitor, timePoint, cache);
                } catch (FunctionEvaluationException e) {
                    logger.log(Level.WARNING, "Problem evaluating performance curve function for competitor "+competitor, e);
                    competitorPerformanceCurve = null;
                }
                final Duration competitorTimeSinceRaceStart = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
                Speed competitorImpliedWind;
                try {
                    competitorImpliedWind = competitorPerformanceCurve.getImpliedWind(competitorTimeSinceRaceStart);
                } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                    logger.log(Level.WARNING, "Problem evaluating performance curve function for competitor " + competitor
                            + " for duration " + competitorTimeSinceRaceStart, e);
                    logger.fine("The performance curve was: "+competitorPerformanceCurve);
                    competitorImpliedWind = null;
                }
                if (competitorImpliedWind != null) {
                    try {
                        result = scratchBoatPerformanceCurve.getAllowancePerCourse(competitorImpliedWind);
                    } catch (FunctionEvaluationException e) {
                        logger.log(Level.WARNING, "Problem evaluating performance curve function on scratch boat "+scratchBoat+
                                " to compute corrected time of competitor "+competitor+" based on her implied wind ", e);
                        logger.fine("The scratch boat performance curve was: "+scratchBoatPerformanceCurve);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected Duration getCalculatedTime(Competitor who, Supplier<Leg> leg, Supplier<Position> estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        final Duration result;
        if (startOfRace == null) {
            result = null;
        } else {
            result = getCorrectedTime(who, startOfRace.plus(totalDurationSinceRaceStart));
        }
        return result;
    }

    @Override
    public ORCPerformanceCurveRankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, CompetitorRankingInfo> competitorRankingInfo = new HashMap<>();
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace != null) {
            final Duration actualRaceDuration = startOfRace.until(timePoint);
            for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
                final Duration correctedTime = getCorrectedTime(competitor, timePoint, cache);
                competitorRankingInfo.put(competitor, new CompetitorRankingInfoImpl(
                        timePoint, competitor, getWindwardDistanceTraveled(competitor, timePoint, cache),
                        actualRaceDuration, correctedTime,
                        correctedTime == null ? null : actualRaceDuration.plus(correctedTime),
                        correctedTime));
            }
        }
        return new ORCPerformanceCurveRankingInfo(timePoint, competitorRankingInfo, cache);
    }

    /**
     * Uses the implied wind of the {@link RankingInfo#getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()} and
     * maps it to a time in {@code competitor}'s performance curve. This tells {@code competitor} in her own time what
     * the time difference is to the boat leading in the race by implied wind.
     */
    @Override
    public Duration getGapToLeaderInOwnTime(RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingCache cache) {
        assert rankingInfo instanceof ORCPerformanceCurveRankingInfo;
        Duration result;
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace != null) {
            final Duration actualRaceDuration = startOfRace.until(rankingInfo.getTimePoint());
            final Competitor leader = rankingInfo.getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead();
            try {
                final ORCPerformanceCurve competitorPerformanceCurve = getPerformanceCurveForPartialCourse(competitor, rankingInfo.getTimePoint(), cache);
                if (competitorPerformanceCurve == null) {
                    result = null;
                } else {
                    final Speed impliedWindOfLeader = getImpliedWind(leader, rankingInfo.getTimePoint(), cache);
                    final Duration allowanceForLeaderInCompetitorsPerformanceCurve = competitorPerformanceCurve.getAllowancePerCourse(impliedWindOfLeader);
                    result = actualRaceDuration.minus(allowanceForLeaderInCompetitorsPerformanceCurve);
                }
            } catch (FunctionEvaluationException | MaxIterationsExceededException e) {
                logger.log(Level.WARNING, "Problem evaluating performance curve for competitor "+competitor+" for time point "+rankingInfo.getTimePoint(), e);
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Duration getLegGapToLegLeaderInOwnTime(TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint,
            RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        assert rankingInfo instanceof ORCPerformanceCurveRankingInfo;
        Duration result;
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (trackedLegOfCompetitor.hasStartedLeg(timePoint) && startOfRace != null) {
            final Duration actualRaceDurationForCompetitor = startOfRace.until(rankingInfo.getTimePoint());
            final ORCPerformanceCurveRankingInfo orcpcsRankingInfo = (ORCPerformanceCurveRankingInfo) rankingInfo;
            final Competitor legLeader = orcpcsRankingInfo.getLeaderInLegByCalculatedTime(trackedLegOfCompetitor.getLeg(), cache);
            final Competitor competitor = trackedLegOfCompetitor.getCompetitor();
            if (competitor == legLeader) {
                result = Duration.NULL;
            } else {
                TimePoint timeForCompetitorImpliedWindCalculation;
                try {
                    timeForCompetitorImpliedWindCalculation = nowOrLegFinishTimeIfFinishedAtTimePoint(trackedLegOfCompetitor, timePoint, cache);
                    final ORCPerformanceCurve competitorPerformanceCurveForLeg = getPerformanceCurveForPartialCourse(competitor, timeForCompetitorImpliedWindCalculation, cache);
                    if (competitorPerformanceCurveForLeg == null) {
                        result = null;
                    } else {
                        final TimePoint timeForLegLeaderImpliedWindCalculation = nowOrLegFinishTimeIfFinishedAtTimePoint(
                                getTrackedRace().getTrackedLeg(legLeader, trackedLegOfCompetitor.getLeg()), timePoint, cache);
                        final Speed legLeaderImpliedWindInOrAtEndOfLeg = getImpliedWind(legLeader, timeForLegLeaderImpliedWindCalculation, cache);
                        if (legLeaderImpliedWindInOrAtEndOfLeg != null) {
                            final Duration correctedTimeOfLegLeaderInCompetitorsPerformanceCurve = competitorPerformanceCurveForLeg.getAllowancePerCourse(legLeaderImpliedWindInOrAtEndOfLeg);
                            result = actualRaceDurationForCompetitor.minus(correctedTimeOfLegLeaderInCompetitorsPerformanceCurve);
                        } else {
                            result = null;
                        }
                    }
                } catch (FunctionEvaluationException | MaxIterationsExceededException e) {
                    logger.log(Level.WARNING, "Problem with performance curve calculation for competitor "+competitor+" or "+legLeader, e);
                    result = null;
                }
            }
        } else {
            result = null; // competitor didn't start leg yet at timePoint
        }
        return result;
    }

    private TimePoint nowOrLegFinishTimeIfFinishedAtTimePoint(TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) throws FunctionEvaluationException {
        final TimePoint timeForImpliedWindCalculation;
        if (trackedLegOfCompetitor.hasFinishedLeg(timePoint)) {
            timeForImpliedWindCalculation = trackedLegOfCompetitor.getFinishTime();
        } else {
            timeForImpliedWindCalculation = timePoint;
        }
        return timeForImpliedWindCalculation;
    }
}
