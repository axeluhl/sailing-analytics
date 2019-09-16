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
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.ranking.AbstractRankingMetric;
import com.sap.sailing.domain.ranking.RankingMetric;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ORCPerformanceCurveRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -7814822523533929816L;
    private static final Logger logger = Logger.getLogger(ORCPerformanceCurveRankingMetric.class.getName());

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
    
    private final RaceLogEventVisitor certificatesFromRaceLogUpdater;
    
    private final RegattaLogEventVisitor certificatesFromRegattaLogUpdater;
    
    public ORCPerformanceCurveRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
        boatsById = initBoatsById();
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
                }
    
                @Override
                public void raceLogDetached(RaceLog raceLog) {
                    raceLog.removeListener(certificatesFromRaceLogUpdater);
                    updateCertificatesFromLogs();
                }
            });
        }
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
                }
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

    /**
     * Implementation approach: compute the implied wind values for all competitors and base a comparator implementation
     * on those values.
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Speed> impliedWindByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            try {
                impliedWindByCompetitor.put(competitor, getImpliedWind(competitor, timePoint));
            } catch (MaxIterationsExceededException | FunctionEvaluationException e) {
                // log and leave entry for competitor empty; this, together with a nullsLast comparator
                // will sort such competitors towards "worse" ranks
                logger.log(Level.WARNING, "Problem trying to determine ORC PCS implied wind for competitor "
                        + competitor + " for time point " + timePoint, e);
            }
        }
        return (c1, c2)->Comparator.nullsLast((Speed impliedWindSpeed1, Speed impliedWindSpeed2)->impliedWindSpeed2.compareTo(impliedWindSpeed1)).
                compare(impliedWindByCompetitor.get(c1), impliedWindByCompetitor.get(c2));
    }

    private Speed getImpliedWind(Competitor competitor, TimePoint timePoint) throws FunctionEvaluationException, MaxIterationsExceededException {
        final Speed result;
        final ORCPerformanceCurveCourse competitorsPartialCourseAtTimePoint = getPartialCourse(competitor, timePoint);
        final ORCCertificate certificate = getCertificate(getTrackedRace().getBoatOfCompetitor(competitor));
        if (certificate != null) {
            final ORCPerformanceCurve performanceCurveForPartialCourse = new ORCPerformanceCurveImpl(certificate, competitorsPartialCourseAtTimePoint);
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

    private ORCPerformanceCurveCourse getPartialCourse(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint timePointOfTosPosition, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getCalculatedTime(Competitor who, Supplier<Leg> leg, Supplier<Position> estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getGapToLeaderInOwnTime(RankingMetric.RankingInfo rankingInfo, Competitor competitor,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getLegGapToLegLeaderInOwnTime(TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint,
            RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    public ORCCertificate getCertificate(Boat boat) {
        return certificates.get(boat);
    }
}
