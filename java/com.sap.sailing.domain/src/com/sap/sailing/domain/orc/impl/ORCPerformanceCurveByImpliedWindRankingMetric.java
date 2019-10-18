package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
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
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.ranking.AbstractRankingMetric;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

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
    
    private Boat boatWithLeastGPH;
    
    private ORCPerformanceCurveCourse totalCourse;
    
    private final Map<Serializable, Boat> boatsById;
    
    private final Map<Serializable, Competitor> competitorsById;
    
    private transient RaceLogEventVisitor certificatesAndCourseAndScratchBoatFromRaceLogUpdater;
    
    private transient RegattaLogEventVisitor certificatesFromRegattaLogUpdater;
    
    /**
     * Updated by an observer pattern that watches all {@link RaceLog}s {@link TrackedRace#getAttachedRaceLogs() attached} to the
     * {@link #getTrackedRace() tracked race} for occurrence and revocations of {@link RaceLogORCScratchBoatEvent}s.
     */
    private Competitor explicitScratchBoat;
    
    private class ORCPerformanceCurveRankingInfo extends AbstractRankingInfoWithCompetitorRankingInfoCache {
        private static final long serialVersionUID = -3578498778702139675L;
        
        /**
         * Uses the {@link ORCPerformanceCurveRankingMetric#getScratchBoat(TimePoint) scratch boat} as the "boat
         * farthest ahead." The default scratch boat is defined as such but may be overridden explicitly.
         */
        public ORCPerformanceCurveRankingInfo(TimePoint timePoint, Competitor competitorFarthestAhead, Map<Competitor, CompetitorRankingInfo> competitorRankingInfo, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
            super(timePoint, competitorRankingInfo, competitorFarthestAhead);
        }
    }
    
    public ORCPerformanceCurveByImpliedWindRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
        boatsById = initBoatsById();
        competitorsById = initCompetitorsById();
        initializeListeners();
        updateCertificatesFromLogs();
        updateCourseFromRaceLogs();
    }

    private void initializeListeners() {
        certificatesAndCourseAndScratchBoatFromRaceLogUpdater = createCertificatesFromRaceLogAndCourseAndScratchBoatUpdater();
        certificatesFromRegattaLogUpdater = createCertificatesFromRegattaLogUpdater();
        if (getTrackedRace() != null) {
            addTrackedRaceListener(getTrackedRace());
            for (final RegattaLog regattaLog : getTrackedRace().getAttachedRegattaLogs()) {
                regattaLog.addListener(certificatesFromRegattaLogUpdater);
            }
            for (final RaceLog raceLog : getTrackedRace().getAttachedRaceLogs()) {
                raceLog.addListener(certificatesAndCourseAndScratchBoatFromRaceLogUpdater);
            }
        }
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        ois.registerValidation(()->initializeListeners(), /* prio */ -1);
    }

    private void addTrackedRaceListener(TrackedRace trackedRace) {
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void regattaLogAttached(RegattaLog regattaLog) {
                regattaLog.addListener(certificatesFromRegattaLogUpdater);
                updateCertificatesFromLogs();
            }
   
            @Override
            public void raceLogAttached(RaceLog raceLog) {
                raceLog.addListener(certificatesAndCourseAndScratchBoatFromRaceLogUpdater);
                updateCertificatesFromLogs();
                updateScratchBoatFromLogs();
                updateCourseFromRaceLogs();
            }
   
            @Override
            public void raceLogDetached(RaceLog raceLog) {
                raceLog.removeListener(certificatesAndCourseAndScratchBoatFromRaceLogUpdater);
                updateCertificatesFromLogs();
                updateScratchBoatFromLogs();
                updateCourseFromRaceLogs();
            }
            
            // see bug 5130: don't add as a course change listener on the course but on the race because
            // only this way will the TrackedRace have aligned its TrackedLeg objects before triggering
            // these hooks.
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
    
    public ORCCertificate getCertificate(Boat boat) {
        return certificates.get(boat);
    }
    
    protected Boat getBoatWithLeastGph() {
        return boatWithLeastGPH;
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

    private RaceLogEventVisitor createCertificatesFromRaceLogAndCourseAndScratchBoatUpdater() {
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
        if (getTrackedRace() != null) {
            final Map<Boat, ORCCertificate> newCertificates = new HashMap<>();
            for (final RegattaLog regattaLog : getTrackedRace().getAttachedRegattaLogs()) {
                newCertificates.putAll(new RegattaLogORCCertificateAssignmentFinder(regattaLog, boatsById).analyze());
            }
            for (final RaceLog raceLog : getTrackedRace().getAttachedRaceLogs()) {
                newCertificates.putAll(new RaceLogORCCertificateAssignmentFinder(raceLog, boatsById).analyze());
            }
            certificates = newCertificates;
            Duration minGPH = new MillisecondsDurationImpl(Long.MAX_VALUE);
            Boat boatWithMinGPH = null;
            for (final Entry<Boat, ORCCertificate> e : certificates.entrySet()) {
                if (e.getValue().getGPH().compareTo(minGPH) < 0) {
                    boatWithMinGPH = e.getKey();
                    minGPH = e.getValue().getGPH();
                }
            }
            boatWithLeastGPH = boatWithMinGPH;
        }
    }
    
    private void updateCourseFromRaceLogs() {
        if (getTrackedRace() != null) {
            final Map<Integer, ORCPerformanceCurveLeg> legsWithDefinitions = new HashMap<>();
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

    private ORCPerformanceCurveCourse getPartialCourse(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final ORCPerformanceCurveCourse result;
        final Leg firstLeg = getTrackedRace().getRace().getCourse().getFirstLeg();
        final Waypoint finish = getTrackedRace().getRace().getCourse().getLastWaypoint();
        final MarkPassing finishMarkPassing;
        if (finish != null && (finishMarkPassing = getTrackedRace().getMarkPassing(competitor, finish)) != null &&
                !finishMarkPassing.getTimePoint().after(timePoint)) {
            // at or beyond finish mark passing; use total course; works also if track is missing or incomplete or broken
            result = cache.getTotalCourse(getTrackedRace(), ()->getTotalCourse());
        } else {
            final TrackedLegOfCompetitor trackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, timePoint);
            if (trackedLegOfCompetitor == null) {
                // Has the competitor finished the race so we can take the total course?
                // trackedLegOfCompetitor is null either if we're before competitor's start time
                // or after competitor's finish time, so if we figure the competitor has started at or before
                // timePoint then the competitor must have finished.
                final TrackedLegOfCompetitor trackedFirstLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, firstLeg);
                if (trackedFirstLegOfCompetitor != null && trackedFirstLegOfCompetitor.hasStartedLeg(timePoint)) {
                    // then we know the competitor has finished the race at timePoint
                    result = cache.getTotalCourse(getTrackedRace(), ()->getTotalCourse());
                } else {
                    // not started the race yet; return empty course
                    result = cache.getTotalCourse(getTrackedRace(), ()->getTotalCourse()).subcourse(0, 0);
                }
            } else {
                // started but not yet finished; compute true partial course
                final ORCPerformanceCurveCourse totalCourse = cache.getTotalCourse(getTrackedRace(), ()->getTotalCourse());
                final int zeroBasedIndexOfCurrentLeg = getTrackedRace().getRace().getCourse().getIndexOfWaypoint(trackedLegOfCompetitor.getLeg().getFrom());
                final ORCPerformanceCurveLeg currentLeg = Util.get(totalCourse.getLegs(), zeroBasedIndexOfCurrentLeg);
                final double shareOfCurrentLeg;
                final LegType legType;
                if (currentLeg.getType().equals(ORCPerformanceCurveLegTypes.WINDWARD_LEEWARD)
                        || currentLeg.getType().equals(ORCPerformanceCurveLegTypes.TWA)) {
                    legType = null;
                } else {
                    legType = LegType.REACHING;
                }
                // use windward projection in case we deem the current leg an upwind or downwind leg
                shareOfCurrentLeg = 1.0
                        - trackedLegOfCompetitor.getWindwardDistanceToGo(legType, timePoint, WindPositionMode.LEG_MIDDLE, cache).divide(
                                trackedLegOfCompetitor.getTrackedLeg().getWindwardDistance(legType, timePoint, cache));
                result = totalCourse.subcourse(zeroBasedIndexOfCurrentLeg, shareOfCurrentLeg);
            }
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
     * but other definitions are possible, such as greatest {@link ORCCertificate#getGPHInSecondsToTheMile() GPH value} or least
     * {@link ORCCertificate#getCDL() CDL value}. An explicit selection can be made using the
     * {@link RaceLogORCScratchBoatEvent} in the {@link RaceLog}. The last unrevoked event in the first {@link RaceLog}
     * in which one is found is used. Only if none of the attached {@link RaceLog}s contains any unrevoked such event,
     * the scratch boat is selected implicitly to be the boat farthest ahead (see
     * {@link #getCompetitorFarthestAhead(TimePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache)}).
     */
    private Competitor getScratchBoat(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
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
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = getImpliedWindByCompetitor(timePoint, cache);
        return (c1, c2)->Comparator.nullsLast((Speed impliedWindSpeed1, Speed impliedWindSpeed2)->impliedWindSpeed2.compareTo(impliedWindSpeed1)).
                compare(impliedWindByCompetitor.get(c1), impliedWindByCompetitor.get(c2));
    }

    protected Map<Competitor, Speed> getImpliedWindByCompetitor(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            impliedWindByCompetitor.put(competitor, cache.getImpliedWind(timePoint, getTrackedRace(), competitor, getImpliedWindSupplier(cache)));
        }
        return impliedWindByCompetitor;
    }
    
    protected ORCPerformanceCurve getPerformanceCurveForPartialCourse(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) throws FunctionEvaluationException {
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

    protected Speed getImpliedWind(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) throws FunctionEvaluationException, MaxIterationsExceededException {
        final Speed result;
        final ORCPerformanceCurve performanceCurveForPartialCourse = cache.getPerformanceCurveForPartialCourse(timePoint, getTrackedRace(), competitor, getPerformanceCurveSupplier(cache));
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
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            Speed impliedWind;
            if (trackedLeg.getTrackedLeg(competitor).hasFinishedLeg(timePoint)) {
                // dedicated time point at leg end; cannot use implied wind from cache
                impliedWind = cache.getImpliedWind(trackedLeg.getTrackedLeg(competitor).getFinishTime(),
                        getTrackedRace(), competitor, getImpliedWindSupplier(cache));
            } else {
                // can use cache; we shall compute for the cache's timePoint:
                impliedWind = cache.getImpliedWind(timePoint, getTrackedRace(), competitor, getImpliedWindSupplier(cache));
            }
            impliedWindByCompetitor.put(competitor, impliedWind);
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
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        Duration result = null;
        final Competitor scratchBoat = cache.getScratchBoat(timePoint, getTrackedRace(), tp->getScratchBoat(tp, cache));
        if (competitor == scratchBoat) {
            // the scratch boat's corrected time is its own time sailed
            result = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
        } else {
            final BiFunction<TimePoint, Competitor, ORCPerformanceCurve> performanceCurveSupplier = getPerformanceCurveSupplier(cache);
            ORCPerformanceCurve scratchBoatPerformanceCurve;
            scratchBoatPerformanceCurve = cache.getPerformanceCurveForPartialCourse(timePoint, getTrackedRace(), scratchBoat, performanceCurveSupplier);
            if (scratchBoatPerformanceCurve != null) {
                ORCPerformanceCurve competitorPerformanceCurve = cache.getPerformanceCurveForPartialCourse(timePoint, getTrackedRace(), competitor, performanceCurveSupplier);
                if (competitorPerformanceCurve != null) {
                    final Duration competitorElapsedTimeSinceRaceStart = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
                    Speed competitorImpliedWind;
                    try {
                        competitorImpliedWind = competitorPerformanceCurve.getImpliedWind(competitorElapsedTimeSinceRaceStart);
                    } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                        logger.log(Level.WARNING, "Problem evaluating performance curve function for competitor " + competitor
                                + " for duration " + competitorElapsedTimeSinceRaceStart, e);
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
                } // else, certificate was not found and result remains null
            }
        }
        return result;
    }

    protected BiFunction<TimePoint, Competitor, Speed> getImpliedWindSupplier(WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return (timePoint, competitor)->{
            try {
                return getImpliedWind(competitor, timePoint, cache);
            } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                logger.log(Level.WARNING, "Problem evaluating performance curve", e);
                return null;
            }
        };
    }

    protected BiFunction<TimePoint, Competitor, ORCPerformanceCurve> getPerformanceCurveSupplier(
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return (timePoint, competitor) -> {
            try {
                return getPerformanceCurveForPartialCourse(competitor, timePoint, cache);
            } catch (FunctionEvaluationException e) {
                logger.log(Level.WARNING, "Problem evaluating performance curve function for "+competitor, e);
                return null;
            }
        };
    }

    @Override
    public ORCPerformanceCurveRankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, CompetitorRankingInfo> competitorRankingInfo = new HashMap<>();
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        final Competitor competitorFarthestAhead = getCompetitorFarthestAhead(timePoint, cache);
        if (startOfRace != null) {
            final Duration actualRaceDuration = startOfRace.until(timePoint);
            final Set<ForkJoinTask<Pair<Competitor, CompetitorRankingInfoImpl>>> futures = new HashSet<>();
            for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
                futures.add(ForkJoinTask.adapt(()->{
                    final Duration correctedTime = getCorrectedTime(competitor, timePoint, cache);
                    return new Pair<>(competitor, new CompetitorRankingInfoImpl(
                            timePoint, competitor, getWindwardDistanceTraveled(competitor, timePoint, cache),
                            actualRaceDuration, correctedTime,
                            getEstimatedActualDurationToCompetitorFarthestAhead(competitor, competitorFarthestAhead, timePoint, cache),
                            correctedTime));
                }).fork());
            }
            for (final ForkJoinTask<Pair<Competitor, CompetitorRankingInfoImpl>> future : futures) {
                Pair<Competitor, CompetitorRankingInfoImpl> resultForCompetitor;
                resultForCompetitor = future.join();
                competitorRankingInfo.put(resultForCompetitor.getA(), resultForCompetitor.getB());
            }
        }
        return new ORCPerformanceCurveRankingInfo(timePoint, competitorFarthestAhead, competitorRankingInfo, cache);
    }

    /**
     * Computes the predicted duration to the boat farthest ahead; use the competitor's implied wind so far and compute
     * its performance curve for the partial course of the boat farthest ahead; apply the current implied wind and take
     * the delta between the resulting allowance and the time sailed so far
     */
    private Duration getEstimatedActualDurationToCompetitorFarthestAhead(Competitor competitor, Competitor competitorFarthestAhead,
            TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final ORCPerformanceCurveCourse courseOfCompetitorFarthestAhead = getPartialCourse(competitorFarthestAhead, timePoint, cache);
        ORCPerformanceCurve performanceCurveForCompetitorToPositionOfCompetitorFarthestAhread;
        Duration result;
        try {
            final ORCCertificate certificate = getCertificate(getTrackedRace().getBoatOfCompetitor(competitor));
            if (certificate != null) {
                performanceCurveForCompetitorToPositionOfCompetitorFarthestAhread = new ORCPerformanceCurveImpl(certificate,
                        courseOfCompetitorFarthestAhead);
                final Speed competitorsCurrentImpliedWind = cache.getImpliedWind(timePoint, getTrackedRace(), competitorFarthestAhead, getImpliedWindSupplier(cache));
                if (competitorsCurrentImpliedWind != null) {
                    final Duration allowanceToPositionOfBoatFarthestAhead = performanceCurveForCompetitorToPositionOfCompetitorFarthestAhread
                            .getAllowancePerCourse(competitorsCurrentImpliedWind);
                    final Duration competitorElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
                    if (competitorElapsedTime == null) { // probably not finished before end-of-tracking
                        result = null;
                    } else {
                        result = allowanceToPositionOfBoatFarthestAhead.minus(competitorElapsedTime);
                    }
                } else {
                    result = null;
                }
            } else {
                result = null; // no certificate found
            }
        } catch (FunctionEvaluationException e) {
            logger.log(Level.SEVERE, "Problem evaluating ORC PCS function", e);
            result = null;
        }
        return result;
    }

    /**
     * Uses the implied wind of the {@link RankingInfo#getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()} and
     * maps it to a time in {@code competitor}'s performance curve. This tells {@code competitor} in her own time what
     * the time difference is to the boat leading in the race by implied wind.
     */
    @Override
    public Duration getGapToLeaderInOwnTime(RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        assert rankingInfo instanceof ORCPerformanceCurveRankingInfo;
        Duration result;
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace != null) {
            final Duration actualRaceDuration = startOfRace.until(rankingInfo.getTimePoint());
            final Competitor leader = rankingInfo.getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead();
            try {
                final ORCPerformanceCurve competitorPerformanceCurve = cache.getPerformanceCurveForPartialCourse(
                        rankingInfo.getTimePoint(), getTrackedRace(), competitor,
                        getPerformanceCurveSupplier(cache));
                if (competitorPerformanceCurve == null) {
                    result = null;
                } else {
                    final Speed impliedWindOfLeader = cache.getImpliedWind(rankingInfo.getTimePoint(), getTrackedRace(),
                            leader, getImpliedWindSupplier(cache));
                    if (impliedWindOfLeader != null) {
                        final Duration allowanceForLeaderInCompetitorsPerformanceCurve = competitorPerformanceCurve.getAllowancePerCourse(impliedWindOfLeader);
                        result = actualRaceDuration.minus(allowanceForLeaderInCompetitorsPerformanceCurve);
                    } else {
                        result = null; // no implied wind for leader could be determined
                    }
                }
            } catch (FunctionEvaluationException e) {
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
            RankingInfo rankingInfo, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
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
                        final Speed legLeaderImpliedWindInOrAtEndOfLeg = cache.getImpliedWind(timeForLegLeaderImpliedWindCalculation, getTrackedRace(), competitor,
                                getImpliedWindSupplier(cache));
                        if (legLeaderImpliedWindInOrAtEndOfLeg != null) {
                            final Duration correctedTimeOfLegLeaderInCompetitorsPerformanceCurve = competitorPerformanceCurveForLeg.getAllowancePerCourse(legLeaderImpliedWindInOrAtEndOfLeg);
                            result = actualRaceDurationForCompetitor.minus(correctedTimeOfLegLeaderInCompetitorsPerformanceCurve);
                        } else {
                            result = null;
                        }
                    }
                } catch (FunctionEvaluationException e) {
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
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) throws FunctionEvaluationException {
        final TimePoint timeForImpliedWindCalculation;
        if (trackedLegOfCompetitor.hasFinishedLeg(timePoint)) {
            timeForImpliedWindCalculation = trackedLegOfCompetitor.getFinishTime();
        } else {
            timeForImpliedWindCalculation = timePoint;
        }
        return timeForImpliedWindCalculation;
    }

    @Override
    protected LegType getLegTypeForRanking(TrackedLeg trackedLeg) {
        final int zeroBasedLegIndex = trackedLeg.getLeg().getZeroBasedIndexOfStartWaypoint();
        final ORCPerformanceCurveLeg orcLeg = Util.get(getTotalCourse().getLegs(), zeroBasedLegIndex);
        return ORCPerformanceCurveLegTypes.getLegType(orcLeg.getType());
    }
}
