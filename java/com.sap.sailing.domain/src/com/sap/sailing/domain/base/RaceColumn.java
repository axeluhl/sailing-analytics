package com.sap.sailing.domain.base;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.RaceColumnListeners;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

/**
 * One or more races that would be noted together in a single column in a {@link Leaderboard}. If the number of
 * competitors of a regatta is too big to have a single start, the set of competitors can be split into several
 * {@link Fleet}s. Still, the regatta has a sequence of "races", only that each such "race" actually consists of several
 * individual races, each tracked separately, with a separate start and a separate field of competitors which are
 * distinct subsets of the regatta's competitors. These races are grouped in a {@link RaceColumn}. The
 * {@link RaceColumn} therefore provides access to the {@link TrackedRace}s by {@link Fleet} and by {@link Competitor}.
 * The {@link TrackedRace}s represent the data of a race. Over the life time of this object it can be assigned one or more
 * {@link TrackedRace}s which then act(s) as data provider to this column. If no tracked race has been assigned, the
 * scores reported by this column will all default to zero.<p>
 * 
 * A {@link RaceColumnListener} can be {@link #addRaceColumnListener added} to receive notifications about tracked races
 * being {@link #setTrackedRace(Fleet, TrackedRace) linked} to this column or unlinked from this column.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface RaceColumn extends Named {
    /**
     * Sets the information object used to access the race column's race logs.
     */
    void setRaceLogInformation(RaceLogStore raceLogStore, RegattaLikeIdentifier regattaLikeParent);
    
    /**
     * Gets the race column's race log associated to the passed fleet. Note that the result may be <code>null</code>
     * particularly for columns in a {@link MetaLeaderboard}.
     * 
     * @param fleet
     * @return the race log or <code>null</code> in case this column belongs to a {@link MetaLeaderboard}
     */
    RaceLog getRaceLog(Fleet fleet);
    
    /**
     * @return the fleets for each of which this column has a single race and therefore optionally a {@link TrackedRace}, in
     * ascending order; best fleets first
     */
    Iterable<? extends Fleet> getFleets();
    
    Fleet getFleetByName(String fleetName);
    
    /**
     * By looking at the tracked races linked to this race column, identify the {@link Fleet} in which <code>competitor</code>
     * races in this column. If the competitor is not found, caused by no tracked races being associated with this race column
     * in which <code>competitor</code> competes, <code>null</code> is returned.
     */
    Fleet getFleetOfCompetitor(Competitor competitor);
    
    /**
     * This does also update the {@link #getRaceIdentifier(Fleet) race identifier} by setting it to <code>null</code> if <code>race</code>
     * is <code>null</code>, and <code>race.getRaceIdentifier()</code> otherwise.
     * 
     * @param fleet the fleet within this column with which to associate <code>race</code>
     */
    void setTrackedRace(Fleet fleet, TrackedRace race);
    
    /**
     * Tells if at least one {@link TrackedRace} is associated with this race column. Short for
     * {@link Util#isEmpty(Iterable) isEmpty(}{@link #getFleets() getFleets())}.
     */
    boolean hasTrackedRaces();

    /**
     * @param fleet
     *            the fleet whose associated tracked race to obtain
     * 
     * @return <code>null</code> if this column does not currently have a tracked race associated for <code>fleet</code>
     *         ; otherwise the tracked race for <code>fleet</code> from where all information relevant to this column
     *         can be obtained. See also {@link #getRaceIdentifier(Fleet)}.
     */
    TrackedRace getTrackedRace(Fleet fleet);
    
    /**
     * If a race is associated with this column for the <code>fleet</code>, the respective {@link RaceDefinition} is returned.
     * Otherwise, <code>null</code> is returned.
     */
    RaceDefinition getRaceDefinition(Fleet fleet);
    
    /**
     * Tries to find a tracked race whose {@link RaceDefinition#getCompetitors() competitors} contain <code>competitor</code>. If
     * no such {@link TrackedRace} is currently associated with this race column, <code>null</code> is returned. No two
     * {@link TrackedRace}s may result because a single competitor can be part of only one fleet and therefore not occur
     * twice in a single {@link RaceColumn}.
     */
    TrackedRace getTrackedRace(Competitor competitor);
    
    /**
     * If this column ever was assigned to a tracked race, that race's identifier can be obtained using this method;
     * otherwise, <code>null</code> is returned.
     * 
     * @param fleet
     *            the fleet for which to obtain the race identifier
     */
    RaceIdentifier getRaceIdentifier(Fleet fleet);
    
    /**
     * Records that this leaderboard column is to be associated with the race identified by <code>raceIdentifier</code>.
     * This does not automatically load the tracked race, but the information may be used to re-associate a tracked race
     * with this column based on its {@link TrackedRace#getRaceIdentifier() race identifier}.
     * 
     * @param fleet
     *            the fleet for which to associate a race by its identifier
     * @param raceIdentifier
     *            the race that should be associated with this column+fleet. It should never be null.
     */
    void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier);
    
    /**
     * A "medal race" cannot be discarded. It's score is doubled during score aggregation.
     */
    boolean isMedalRace();
    
    /**
     * Constructs a key for maps storing corrections such as score corrections and max points reasons.
     */
    com.sap.sse.common.Util.Pair<Competitor, RaceColumn> getKey(Competitor competitor);

    RaceColumnListeners getRaceColumnListeners();

    void removeRaceColumnListener(RaceColumnListener listener);

    void addRaceColumnListener(RaceColumnListener listener);

    /**
     * Releases the {@link TrackedRace} previously set by {@link #setTrackedRace(Fleet, TrackedRace)} but leaves the
     * {@link #getRaceIdentifier(Fleet) race identifier} untouched(!). Therefore, the {@link TrackedRace} may be garbage
     * collected but may be re-resolved for this column using the race identifier at a later time.
     * 
     * @param fleet
     *            the fleet for which to release its tracked race
     */
    void releaseTrackedRace(Fleet fleet);
    
    /**
     * Usually, the scores in each leaderboard column count as they are for the overall score. However, if a column is
     * a medal race column it usually counts double. Under certain circumstances, columns may also count with factors different
     * from 1 or 2. For example, we've seen cases in the Extreme Sailing Series where the race committee defined that in the
     * overall series leaderboard the last two columns each count 1.5 times their scores.
     */
    double getFactor();
    
    /**
     * By default, a competitor's total score is computed by summing up the non-discarded total points of each race
     * across the leaderboard, considering the {@link RaceColumn#getFactor() column factors}. Some race columns,
     * however, are defined such that participating competitors start with a zero score from this race column on. If
     * this method returns <code>true</code>, this column advises the leaderboard and scoring scheme to start counting
     * the total points at this column with zero.
     */
    boolean isStartsWithZeroScore();
    
    boolean isDiscardable();
    
    boolean isCarryForward();

    /**
     * @param factor if <code>null</code>, {@link #getFactor()} will again compute a default value; otherwise, {@link #getFactor()} will
     * then return the double value of <code>factor</code>.
     */
    void setFactor(Double factor);

    /**
     * If <code>null</code>, the {@link #getFactor() factor} defaults to 1 for non-medal and {@link #DEFAULT_MEDAL_RACE_FACTOR} for
     * medal races. Otherwise, the explicit factor is used.
     */
    Double getExplicitFactor();

    /**
     * Returns the race log identifier associated with this fleet and race log
     */
    RaceLogIdentifier getRaceLogIdentifier(Fleet fleet);

    /**
     * Reload the {@link RaceLog} for this column with the given fleet
     */
    void reloadRaceLog(Fleet fleet);

    /**
     * Remove the association between a race and a column. This is different from
     * {@link RaceColumn#releaseTrackedRace(Fleet)} because it will also remove the
     * association from database.
     * 
     * @param fleet
     */
    void removeRaceIdentifier(Fleet fleet);

    /**
     * While set to true, any serialization in the current thread will not include the tracked races. Make sure to set
     * back to false, after serialization. (in finally block)
     * 
     * @param flagValue
     *            set to false for default behavior, set to true to exclude tracked races
     */
    public void setMasterDataExportOngoingThreadFlag(boolean flagValue);

    /**
     * Usually, when a regatta has split fleets that are {@link Fleet#getOrdering() ordered}, a competitor participating in a
     * better fleet is always scored better than all competitors participating in worse fleets. However, under some circumstances
     * it may be desirable to model a regatta series such that the fleet pertinence does not lead to a persistent scoring ordering
     * throughout the regatta. For example, the Extreme Sailing Series Knock-Out Races use ordered fleets, but the fleet that a
     * competitor qualified for does not decide about total regatta ranking. It may, however, decide for in-column ranking. See also
     * {@link #isSplit}.
     */
    boolean isTotalOrderDefinedByFleet();

    /**
     * When a column has more than one fleet, there are two different options for scoring it. Either the scoring scheme is applied
     * to the sequence of competitors one gets when first ordering the competitors by fleets and then within each fleet by their
     * rank in the fleet's race; or the scoring scheme is applied to each fleet separately, leading to the best score being awarded
     * in the column as many times as there are fleets in the column. For the latter case, this method returns <code>true</code>.
     */
    boolean hasSplitFleetContiguousScoring();

    boolean hasSplitFleets();
}
