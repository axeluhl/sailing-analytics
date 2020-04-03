package com.sap.sailing.domain.tractracadapter.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbstractFinishPositioningListFinder.CompetitorResultsAndTheirCreationTimePoints;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningConfirmedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.event.RaceCompetitorStatusType;
import com.tractrac.model.lib.api.event.RaceStatusType;

/**
 * A service that understands the different {@link IRaceCompetitor#getStatus() competitor statuses} and the
 * {@link IRace#getStatus() race status} and can reconcile them with the {@link RaceLog} of a {@link TrackedRace} such
 * that afterwards the {@link RaceLog} is guaranteed to describe the competitor status accordingly. When the
 * reconciliation is requested and the {@link RaceLog} already represents the competitor status appropriately, no
 * changes will be applied to the race log.<p>
 * 
 * When you create an object of this type, make sure to inform it about race logs being attached to / detached from
 * the {@link TrackedRace} once the tracked race is known.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceAndCompetitorStatusWithRaceLogReconciler {
    private final DomainFactory domainFactory;
    private final RaceLogResolver raceLogResolver;
    private final LogEventAuthorImpl raceLogEventAuthor;
    private final IRace tractracRace;
    private final Map<Pair<TrackedRace, RaceLog>, RaceLogListener> raceLogListeners;
    private final static Map<RaceStatusType, Flags> flagForRaceStatus;
    
    static {
        flagForRaceStatus = new HashMap<>();
        flagForRaceStatus.put(RaceStatusType.ABANDONED, Flags.NOVEMBER);
        flagForRaceStatus.put(RaceStatusType.POSTPONED, Flags.AP);
        flagForRaceStatus.put(RaceStatusType.GENERAL_RECALL, Flags.FIRSTSUBSTITUTE);
    }
    
    /**
     * Handles those race log events that may have an impact on the reconciliation process, including revocations and
     * pass changes, and invokes
     * {@link RaceAndCompetitorStatusWithRaceLogReconciler#reconcileCompetitorStatus(IRaceCompetitor, TrackedRace)} or
     * {@link RaceAndCompetitorStatusWithRaceLogReconciler#reconcileRaceStatus(IRace, TrackedRace)} or both, depending
     * on the type of even. Instances of this type are registered with race logs because the enclosing
     * {@link RaceAndCompetitorStatusWithRaceLogReconciler} object has to get informed about race log attachments/detachments
     * in its {@link RaceAndCompetitorStatusWithRaceLogReconciler#raceLogAttached(TrackedRace, RaceLog)} and
     * {@link RaceAndCompetitorStatusWithRaceLogReconciler#raceLogDetached(TrackedRace, RaceLog)} methods.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class RaceLogListener extends BaseRaceLogEventVisitor {
        private final RaceLog raceLog;
        private final TrackedRace trackedRace;
        
        public RaceLogListener(TrackedRace trackedRace, RaceLog raceLog) {
            super();
            this.raceLog = raceLog;
            this.trackedRace = trackedRace;
            reconcileRaceStatus(tractracRace, trackedRace);
            reconcileAllCompetitors(trackedRace);
        }

        private void reconcileAllCompetitors(TrackedRace trackedRace) {
            for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                if (competitor.getId() instanceof UUID) {
                    final IRaceCompetitor raceCompetitor = tractracRace.getRaceCompetitor((UUID) competitor.getId());
                    reconcileCompetitorStatus(raceCompetitor, trackedRace);
                }
            }
        }

        @Override
        public void visit(RaceLogFlagEvent event) {
            reconcileRaceStatus(tractracRace, trackedRace);
        }

        @Override
        public void visit(RaceLogPassChangeEvent event) {
            reconcileRaceStatus(tractracRace, trackedRace);
            reconcileAllCompetitors(trackedRace);
        }

        @Override
        public void visit(RaceLogFinishPositioningConfirmedEvent event) {
            reconcileCompetitorsWithResults(event);
        }

        @Override
        public void visit(RaceLogRevokeEvent event) {
            final RaceLogEvent revokedEvent = raceLog.getEventById(event.getRevokedEventId());
            if (revokedEvent != null) {
                if (revokedEvent instanceof RaceLogFinishPositioningConfirmedEvent) {
                    final RaceLogFinishPositioningConfirmedEvent revokedResultsEvent = (RaceLogFinishPositioningConfirmedEvent) revokedEvent;
                    reconcileCompetitorsWithResults(revokedResultsEvent);
                } else if (revokedEvent instanceof RaceLogFlagEvent || revokedEvent instanceof RaceLogPassChangeEvent) {
                    reconcileRaceStatus(tractracRace, trackedRace);
                }
            }
        }

        private void reconcileCompetitorsWithResults(final RaceLogFinishPositioningConfirmedEvent resultsEvent) {
            for (final CompetitorResult competitorResult : resultsEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()) {
                final Serializable competitorId = competitorResult.getCompetitorId();
                if (competitorId instanceof UUID) {
                    reconcileCompetitorStatus(tractracRace.getRaceCompetitor((UUID) competitorId), trackedRace);
                }
            }
        }
    }
    
    public RaceAndCompetitorStatusWithRaceLogReconciler(DomainFactory domainFactory, RaceLogResolver raceLogResolver, IRace tractracRace) {
        super();
        this.domainFactory = domainFactory;
        this.raceLogResolver = raceLogResolver;
        this.tractracRace = tractracRace;
        raceLogListeners = Collections.synchronizedMap(new HashMap<>());
        raceLogEventAuthor = new LogEventAuthorImpl(getClass().getName(), 1);
    }
    
    public void raceLogAttached(TrackedRace trackedRace, RaceLog raceLog) {
        final RaceLogListener listener = new RaceLogListener(trackedRace, raceLog);
        raceLog.addListener(listener);
        raceLogListeners.put(new Pair<>(trackedRace, raceLog), listener);
    }
    
    public void raceLogDetached(TrackedRace trackedRace, RaceLog raceLog) {
        final RaceLogListener listener = raceLogListeners.remove(new Pair<>(trackedRace, raceLog));
        if (listener != null) {
            raceLog.removeListener(listener);
        }
    }

    /**
     * The following race status types are currently available on the TracTrac side (see {@link RaceStatusType}):
     * <ul>
     * <li>NONE(0)</li>
     * <li>START(4)</li>
     * <li>RACING(5)</li>
     * <li>UNOFFICIAL(7)</li>
     * <li>ABANDONED(8)</li>
     * <li>OFFICIAL(9)</li>
     * <li>GENERAL_RECALL(10)</li>
     * <li>POSTPONED(11)</li>
     * </ul>
     * We are interested in status transitions that need to be reflected by an "N" ("November", abort), "AP" (answering
     * pennant, postponement), or 1st substitute (general recall) flag status in the race log. The TracTrac-provided
     * status transition has a time stamp on it (see {@link IRace#getStatusTime()}), and so would any aborting flag
     * event in a {@link RaceLog} as well as any pass change event. If the TracTrac status time is later than the last
     * race log-based aborting flag event from the current pass and the TracTrac status is none of {@code ABANDONED},
     * {@code GENERAL_RECALL} or {@code POSTPONED}, a new pass will be established in the race log. If the TracTrac
     * status is any of {@code ABANDONED}, {@code GENERAL_RECALL} or {@code POSTPONED}, and the race log has not the
     * matching aborting flag in the current pass, and the TracTrac status update time is later than the last race log
     * status, the corresponding race log event that represents {@code ABANDONED}, {@code GENERAL_RECALL} or
     * {@code POSTPONED}, respectively, will be appended to the {@link #getDefaultRaceLog(TrackedRace) default race
     * log}.
     * <p>
     * 
     * There is no API currently that allows us to determine the start mode flag. Manual intervention would be required
     * if a non-default start mode flag is to be shown.
     * <p>
     * 
     * If multiple race logs are attached, a "default" race log will be determined, e.g., based on the one that already
     * has the most events in it. See {@link #getDefaultRaceLog}.
     */
    public void reconcileRaceStatus(IRace tractracRace, TrackedRace trackedRace) {
        final RaceStatusType raceStatus = tractracRace.getStatus();
        final MillisecondsTimePoint raceStatusUpdateTime = new MillisecondsTimePoint(tractracRace.getStatusTime());
        RaceLogFlagEvent abortingFlagEvent = null;
        for (final RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            if (abortingFlagEvent == null) {
                final AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
                abortingFlagEvent = abortingFlagFinder.analyze();
            }
        }
        final RaceLog defaultRaceLog = getDefaultRaceLog(trackedRace);
        if (abortingFlagEvent != null && !isAbortedState(raceStatus) && raceStatusUpdateTime.after(abortingFlagEvent.getLogicalTimePoint())) {
            startNewPass(raceStatusUpdateTime, defaultRaceLog);
        } else if (isAbortedState(raceStatus) &&
                (abortingFlagEvent == null || (!abortingFlagMatches(raceStatus, abortingFlagEvent.getUpperFlag()) &&
                                               raceStatusUpdateTime.after(abortingFlagEvent.getLogicalTimePoint())))) {
            defaultRaceLog.add(new RaceLogFlagEventImpl(raceStatusUpdateTime, raceLogEventAuthor, defaultRaceLog.getCurrentPassId(),
                    flagForRaceStatus.get(raceStatus), /* lower flag */ null, /* is displayed */ true));
            startNewPass(raceStatusUpdateTime, defaultRaceLog);
        }
        // TODO bug5154 check IRace.getStatus() and copy all competitor ranks to the leaderboard once the race status reached RaceStatusType.OFFICIAL
    }

    protected void startNewPass(final MillisecondsTimePoint timePointForStartOfNewPass, final RaceLog raceLog) {
        raceLog.add(new RaceLogPassChangeEventImpl(timePointForStartOfNewPass, raceLogEventAuthor, raceLog.getCurrentPassId() + 1));
    }

    private boolean abortingFlagMatches(RaceStatusType raceStatus, Flags upperFlag) {
        return flagForRaceStatus.get(raceStatus) == upperFlag;
    }

    private boolean isAbortedState(RaceStatusType raceStatus) {
        return raceStatus == RaceStatusType.ABANDONED || raceStatus == RaceStatusType.GENERAL_RECALL || raceStatus == RaceStatusType.POSTPONED;
    }
    
    /**
     * Maps TracTrac competitor status to the local domain model's penalty codes
     * 
     * @return {@code null} in case the TracTrac code cannot be mapped to any {@link MaxPointsReason} reasonably
     */
    private MaxPointsReason getMaxPointsReason(RaceCompetitorStatusType raceCompetitorStatusType) {
        MaxPointsReason result;
        switch (raceCompetitorStatusType) {
        case ABANDONED:
            result = MaxPointsReason.NONE; // TODO bug 5154: find out what ABANDONED means and if/how we can translate it to a MaxPointsReason
            break;
        case BFD:
            result = MaxPointsReason.BFD;
            break;
        case DISQUALIFIED:
            result = MaxPointsReason.DSQ;
            break;
        case DNC:
            result = MaxPointsReason.DNC;
            break;
        case DNF:
            result = MaxPointsReason.DNF;
            break;
        case DONT_RACE:
            result = MaxPointsReason.DNS; // TODO bug 5154: find out what DONT_RACE means and if/how we can translate it to a MaxPointsReason; is it DNS?
            break;
        case FIN:
            result = MaxPointsReason.NONE; // TODO bug 5154: find out what FIN means and if/how we can translate it to a MaxPointsReason; does it mean the competitor finished properly?
            break;
        case FINISH_CONFIRMED:
            result = MaxPointsReason.NONE; // TODO bug 5154: find out what FINISH_CONFIRMED means and if/how we can translate it to a MaxPointsReason; does it mean the competitor finished properly?
            break;
        case MIS:
            result = MaxPointsReason.NONE; // TODO bug 5154: find out what MIS means and if/how we can translate it to a MaxPointsReason; does it mean the competitor is "missing?"
            break;
        case NO_COLLECT:
            result = MaxPointsReason.NONE; // TODO bug 5154: find out what NO_COLLECT means and if/how we can translate it to a MaxPointsReason
            break;
        case NO_DATA:
            result = MaxPointsReason.NONE;
            break;
        case OCS:
            result = MaxPointsReason.OCS;
            break;
        case RACING:
            result = MaxPointsReason.NONE;
            break;
        case RETIRED:
            result = MaxPointsReason.RET;
            break;
        case UFD:
            result = MaxPointsReason.UFD;
            break;
        default:
            result = MaxPointsReason.NONE;
            break;
        }
        return result;
    }

    /**
     * If no race log is attached to the {@code trackedRace}, {@code null} is returned. If exactly one race log
     * is attached, it is returned. If multiple race logs are attached, the one with the most entries is returned.
     */
    private RaceLog getDefaultRaceLog(TrackedRace trackedRace) {
        final RaceLog result;
        if (Util.isEmpty(trackedRace.getAttachedRaceLogs())) {
            result = null;
        } else {
            result = Util.stream(trackedRace.getAttachedRaceLogs()).max((rl1, rl2)->Integer.compare(rl1.size(), rl2.size())).get();
        }
        return result;
    }
    
    /**
     * If an official finish time or rank exists on the {@link IRaceCompetitor} but not in any of the {@link TrackedRace}'s
     * {@link RaceLog}-based results, or the corresponding TracTrac information is newer than the race log-based result,
     * a new race log entry of type {@link RaceLogFinishPositioningListConfirmedEvent} will be created that describes
     * the results as obtained from the {@link IRaceCompetitor}'s {@link IRaceCompetitor#getOfficialFinishTime()} and
     * {@link IRaceCompetitor#getOfficialRank()} methods. If the {@link IRaceCompetitor} has a status update time but
     * empty results and there is a {@link CompetitorResult} for that competitor coming from the race log, that competitor
     * result will be "invalidated" / "revoked" by adding a new {@link CompetitorResult} at the end of the race log that
     * has an empty finishing time and zero rank.
     */
    public void reconcileCompetitorStatus(IRaceCompetitor raceCompetitor, TrackedRace trackedRace) {
        final IRace tractracRace = raceCompetitor.getRace();
        final RaceStatusType raceStatus = tractracRace.getStatus();
        final int officialRank = raceCompetitor.getOfficialRank(); // TODO accept rank information only if race status is OFFICIAL
        final long officialFinishingTime = raceCompetitor.getOfficialFinishTime();
        final long timePointForStatusEvent = raceCompetitor.getStatusTime();
        if (timePointForStatusEvent != 0) {
            // there is an official result for the competitor on TracTrac's side
            // TODO bug5154: shall we really ignore that result in case the raceStatus is not OFFICIAL?
            if (raceStatus == RaceStatusType.OFFICIAL) {
                // find out if we already have this information represented in the race log(s) and if not if the TracTrac information is newer:
                final Competitor competitor = domainFactory.resolveCompetitor(raceCompetitor.getCompetitor());
                Pair<CompetitorResult, TimePoint> resultFromRaceLogAndItsCreationTimePoint = getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
                final RaceCompetitorStatusType competitorStatus = raceCompetitor.getStatus();
                final MaxPointsReason officialMaxPointsReason = getMaxPointsReason(competitorStatus);
                final MillisecondsTimePoint officialResultTime = new MillisecondsTimePoint(timePointForStatusEvent);
                // accept even OFFICIAL results only if at least one of officialRank or officialFinishingTime is
                // provided:
                // Jorge Piera Llodra on 2020-04-01: "If both the finishing time and the rank are not defined means that
                // we don’t have these values. In fact, we only have these values for some races of the test event in
                // Enoshima last year.
                // If the race is OFFICIAL but these values are empty, just ignore them."
                if ((officialFinishingTime != 0 || officialRank != 0) && (resultFromRaceLogAndItsCreationTimePoint == null
                        || (resultFromRaceLogAndItsCreationTimePoint.getA().getOneBasedRank() != officialRank
                         || ((resultFromRaceLogAndItsCreationTimePoint.getA().getFinishingTime() == null) ? 0
                                : resultFromRaceLogAndItsCreationTimePoint.getA().getFinishingTime().asMillis()) != officialFinishingTime
                         || resultFromRaceLogAndItsCreationTimePoint.getA().getMaxPointsReason() != officialMaxPointsReason)
                        && resultFromRaceLogAndItsCreationTimePoint.getB().before(officialResultTime))) {
                    // We have an official statement from TracTrac, rank or finishing time or penalty varies and is newer
                    // than the last thing we see in the race log (including we may not have anything in the race
                    // log for the results of that competitor at all yet).
                    // --> Write the official result to the race log
                    final CompetitorResult resultForRaceLog = new CompetitorResultImpl(competitor.getId(), competitor.getName(),
                            competitor.getShortName(),
                            /* boat name: */ ((competitor.hasBoat() ? ((CompetitorWithBoat) competitor).getBoat().getName() : competitor.getShortInfo())),
                            /* boatSailId */ ((competitor.hasBoat() ? ((CompetitorWithBoat) competitor).getBoat().getSailID() : competitor.getShortInfo())),
                            officialRank, officialMaxPointsReason, /* score null means let the scoring system calculate it */ null,
                            officialFinishingTime == 0 ? null : new MillisecondsTimePoint(officialFinishingTime),
                                    "Official results from TracTrac connector", MergeState.OK);
                    final CompetitorResults resultsForRaceLog = new CompetitorResultsImpl();
                    resultsForRaceLog.add(resultForRaceLog);
                    final RaceLog defaultRaceLog = getDefaultRaceLog(trackedRace);
                    defaultRaceLog.add(new RaceLogFinishPositioningConfirmedEventImpl(officialResultTime, officialResultTime,
                            new LogEventAuthorImpl("Official TracTrac Result Provider", 1), // equally important as race officer on water
                            UUID.randomUUID(), defaultRaceLog.getCurrentPassId(), resultsForRaceLog));
                }
            }
        }
    }

    private Pair<CompetitorResult, TimePoint> getRaceLogResultAndCreationTimePointForCompetitor(TrackedRace trackedRace,
            final Competitor competitor) {
        Pair<CompetitorResult, TimePoint> resultFromRaceLogAndItsCreationTimePoint = null;
        for (final RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            final ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(raceLogResolver, raceLog);
            final CompetitorResultsAndTheirCreationTimePoints results = raceState.getConfirmedFinishPositioningList();
            if (results.getCompetitorResults() != null) {
                final Optional<CompetitorResult> result = StreamSupport.stream(results.getCompetitorResults().spliterator(), /* parallel */ false)
                        .filter(r -> Util.equalsWithNull(competitor.getId(), r.getCompetitorId())).findAny();
                if (result.isPresent()) {
                    resultFromRaceLogAndItsCreationTimePoint = new Pair<>(result.get(), results.getCreationTimePointOfResultForCompetitorWithId(result.get().getCompetitorId()));
                    break;
                }
            }
        }
        return resultFromRaceLogAndItsCreationTimePoint;
    }
}
