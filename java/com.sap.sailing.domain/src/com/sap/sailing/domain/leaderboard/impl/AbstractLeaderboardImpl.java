package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

/**
 * Abstract leaderboard implementation that already knows about carried points, competitor display name re-keying,
 * score corrections and result discarding rules. It manages a set of registered {@link RaceColumnListener}s and
 * is itself one. All events received this way are forwarded to all {@link RaceColumnListener}s subscribed. This can
 * be used to subscribe a concrete leaderboard implementation to the data structure providing the actual race columns
 * in order to be notified whenever the set of {@link TrackedRace}s attached to the leaderboard changes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractLeaderboardImpl extends AbstractSimpleLeaderboardImpl {
    private static final long serialVersionUID = -328091952760083438L;

    /**
     * Cache for the combined competitors of this leaderboard; taken from the {@link TrackedRace#getRace() races of the
     * tracked races} associated with this leaderboard. Updated when the set of tracked races changes.
     */
    private transient Iterable<Competitor> allCompetitorsCache;

    /**
     * @param scoreComparator the comparator to use to compare basic scores, such as net points
     * @param name must not be <code>null</code>
     */
    public AbstractLeaderboardImpl(ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(resultDiscardingRule);
    }
    
    @Override
    public Fleet getFleet(String fleetName) {
        for (RaceColumn raceColumn : getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                if (fleet.getName().equals(fleetName)) {
                    return fleet;
                }
            }
        }
        return null;
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
        for (RaceColumn r : getRaceColumns()) {
            for (Fleet fleet : r.getFleets()) {
                TrackedRace trackedRace = r.getTrackedRace(fleet);
                if (trackedRace != null) {
                    trackedRaces.add(trackedRace);
                }
            }
        }
        return Collections.unmodifiableSet(trackedRaces);
    }

    @Override
    public Iterable<Competitor> getAllCompetitors() {
        if (allCompetitorsCache == null) {
            Set<Competitor> result = new HashSet<Competitor>();
            synchronized (this) {
                for (TrackedRace r : getTrackedRaces()) {
                    for (Competitor c : r.getRace().getCompetitors()) {
                        result.add(c);
                    }
                }
            }
            allCompetitorsCache = result;
        }
        return allCompetitorsCache;
    }
    
    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        for (Competitor competitor : getAllCompetitors()) {
            if (competitor.getId().toString().equals(idAsString)) {
                return competitor;
            }
        }
        return null;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        final TrackedRace trackedRace = race.getTrackedRace(competitor);
        return trackedRace == null ? 0
                : trackedRace.hasStarted(timePoint) ? improveByDisqualificationsOfBetterRankedCompetitors(race, trackedRace, timePoint, trackedRace
                        .getRank(competitor, timePoint)) : 0;
    }

    /**
     * Per competitor disqualified ({@link ScoreCorrection} has a {@link MaxPointsReason} for the competitor that has
     * <code>{@link MaxPointsReason#isAdvanceCompetitorsTrackedWorse()}==true</code>), all competitors ranked worse by
     * the tracking system need to have their rank corrected by one.
     * 
     * @param trackedRace
     *            the race to which the rank refers; look for disqualifications / max points reasons in this column
     * @param timePoint
     *            time point at which to consider disqualifications (not used yet because currently we don't remember
     *            <em>when</em> a competitor was disqualified)
     * @param rank
     *            a competitors rank according to the tracking system
     * 
     * @return the unmodified <code>rank</code> if no disqualifications for better-ranked competitors exist for
     *         <code>race</code>, or otherwise a rank improved (lowered) by the number of disqualifications of
     *         competitors whose tracked rank is better (lower) than <code>rank</code>.
     */
    private int improveByDisqualificationsOfBetterRankedCompetitors(RaceColumn raceColumn, TrackedRace trackedRace,
            TimePoint timePoint, int rank) throws NoWindException {
        int correctedRank = rank;
        List<Competitor> competitorsFromBestToWorst = trackedRace.getCompetitorsFromBestToWorst(timePoint);
        int betterCompetitorRank = 1;
        Iterator<Competitor> ci = competitorsFromBestToWorst.iterator();
        while (betterCompetitorRank < rank && ci.hasNext()) {
            Competitor betterTrackedCompetitor = ci.next();
            MaxPointsReason maxPointsReasonForBetterCompetitor = getScoreCorrection().getMaxPointsReason(betterTrackedCompetitor, raceColumn, timePoint);
            if (maxPointsReasonForBetterCompetitor != null && maxPointsReasonForBetterCompetitor != MaxPointsReason.NONE &&
                    maxPointsReasonForBetterCompetitor.isAdvanceCompetitorsTrackedWorse()) {
                correctedRank--;
            }
            betterCompetitorRank++;
        }
        return correctedRank;
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        allCompetitorsCache = null;
        super.trackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        allCompetitorsCache = null;
        super.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }
    
    // Note: no need to redefine isMedalRaceChanged because that doesn't affect the competitorsCache

    @Override
    public Long getDelayToLiveInMillis() {
        TimePoint startOfLatestRace = null;
        Long delayToLiveInMillisForLatestRace = null;
        for (RaceColumn raceColumn : getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    if (startOfLatestRace == null
                            || (trackedRace.getStartOfRace() != null && trackedRace.getStartOfRace().compareTo(startOfLatestRace) > 0)) {
                        delayToLiveInMillisForLatestRace = trackedRace.getDelayToLiveInMillis();
                    }
                }
            }
        }
        return delayToLiveInMillisForLatestRace;
    }

}
