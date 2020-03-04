package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util;
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
 * changes will be applied to the race log.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceAndCompetitorStatusWithRaceLogReconciler {
    private final DomainFactory domainFactory;
    private final RaceLogResolver raceLogResolver;
    private final LogEventAuthorImpl raceLogEventAuthor;
    private final static Map<RaceStatusType, Flags> flagForRaceStatus;
    
    static {
        flagForRaceStatus = new HashMap<>();
        flagForRaceStatus.put(RaceStatusType.ABANDONED, Flags.NOVEMBER);
        flagForRaceStatus.put(RaceStatusType.POSTPONED, Flags.AP);
        flagForRaceStatus.put(RaceStatusType.GENERAL_RECALL, Flags.FIRSTSUBSTITUTE);
    }
    
    public RaceAndCompetitorStatusWithRaceLogReconciler(DomainFactory domainFactory, RaceLogResolver raceLogResolver) {
        super();
        this.domainFactory = domainFactory;
        this.raceLogResolver = raceLogResolver;
        raceLogEventAuthor = new LogEventAuthorImpl(getClass().getName(), 1);
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
     * If an official finish time exists on the {@link IRaceCompetitor} but not in any of the {@link TrackedRace}'s
     * {@link RaceLog}-based results, or the corresponding TracTrac information is newer than the race log-based result,
     * a new race log entry of type {@link RaceLogFinishPositioningListConfirmedEvent} will be created that describes
     * the results as obtained from the {@link IRaceCompetitor}'s {@link IRaceCompetitor#getOfficialFinishTime()} and
     * {@link IRaceCompetitor#getOfficialRank()} methods. If the {@link IRaceCompetitor} has a status update time but
     * empty results and there is a {@link CompetitorResult} for that competitor coming from the race log, that competitor
     * result will be "invalidated" / "revoked" by adding a new {@link CompetitorResult} at the end of the race log that
     * has an empty finishing time and zero rank.
     * 
     * TODO bug5154 continue here...
     * 
     * @param raceCompetitor
     * @param trackedRace
     */
    public void reconcileCompetitorStatus(IRaceCompetitor raceCompetitor, TrackedRace trackedRace) {
        final Competitor competitor = domainFactory.resolveCompetitor(raceCompetitor.getCompetitor());
        final RaceCompetitorStatusType competitorStatus = raceCompetitor.getStatus();
        final int officialRank = raceCompetitor.getOfficialRank(); // TODO accept rank information only if race status is OFFICIAL
        final long officialFinishingTime = raceCompetitor.getOfficialFinishTime();
        final long timePointForStatusEvent = raceCompetitor.getStatusTime();
        for (final RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            final ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(raceLogResolver, raceLog);
            // FIXME we would need the time point of the RaceLogFinishPositioningConfirmedEvent in order to see if the timePointForStatusEvent indicates a newer update
            final CompetitorResults results = raceState.getConfirmedFinishPositioningList();
            final Optional<CompetitorResult> result = StreamSupport.stream(results.spliterator(), /* parallel */ false)
                    .filter(r -> Util.equalsWithNull(competitor.getId(), r.getCompetitorId())).findAny();
            if (result.isPresent()) {
                if (result.get().getOneBasedRank() != officialRank ||
                    ((result.get().getFinishingTime()==null)?0:result.get().getFinishingTime().asMillis()) != officialFinishingTime) {
                    // rank or finishing time varies
                }
            }
            // TODO bug5154 continue here, checking for changes in the IRaceCompetitor status, official rank and official finishing time
        }
    }
}
