package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Optional;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Util;
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
    
    public RaceAndCompetitorStatusWithRaceLogReconciler(DomainFactory domainFactory, RaceLogResolver raceLogResolver) {
        super();
        this.domainFactory = domainFactory;
        this.raceLogResolver = raceLogResolver;
    }

    public void reconcileRaceStatus(IRace tractracRace, TrackedRace trackedRace) {
        final RaceStatusType status = tractracRace.getStatus();
        // TODO bug5154 check IRace.getStatus() and somewhere update status once it reached RaceStatusType.OFFICIAL
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
        final RaceCompetitorStatusType status = raceCompetitor.getStatus();
        final int officialRank = raceCompetitor.getOfficialRank();
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
