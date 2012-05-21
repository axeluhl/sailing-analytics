package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * One or more races that would be noted together in a single column in a {@link Leaderboard}. If the number of
 * competitors of a regatta is too big to have a single start, the set of competitors can be split into several
 * {@link Fleet}s. Still, the regatta has a sequence of "races", only that each such "race" actually consists of several
 * individual races, each tracked separately, with a separate start and a separate field of competitors which are
 * distinct subsets of the regatta's competitors. These races are grouped in a {@link RaceColumn}. The
 * {@link RaceColumn} therefore provides access to the {@link TrackedRace}s by {@link Fleet} and by {@link Competitor}.
 * The {@link TrackedRace}s represent the data of a race. Over the life time of this object it can be assigned one or more
 * {@link TrackedRace}s which then act(s) as data provider to this column. If no tracked race has been assigned, the
 * scores reported by this column will all default to zero.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface RaceColumn extends Named {
    /**
     * @return the fleets for each of which this column has a single race and therefore optionally a {@link TrackedRace}
     */
    Iterable<Fleet> getFleets();
    
    Fleet getFleetByName(String fleetName);
    
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
     * Tries to find a tracked race whose {@link RaceDefinition#getCompetitors() competitors} contain <code>competitor</code>. If
     * no such {@link TrackedRace} is currently associated with this race column, <code>null</code> is returned.
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
     */
    void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier);
    
    /**
     * A "medal race" cannot be discarded. It's score is doubled during score aggregation.
     */
    boolean isMedalRace();
    
    void setIsMedalRace(boolean isMedalRace);
    
    void setName(String newName);
    
    /**
     * Constructs a key for maps storing corrections such as score corrections and max points reasons.
     */
    Pair<Competitor, RaceColumn> getKey(Competitor competitor);

    /**
     * Releases the {@link TrackedRace} previously set by {@link #setTrackedRace(Fleet, TrackedRace)} but leaves the
     * {@link #getRaceIdentifier(Fleet) race identifier} untouched. Therefore, the {@link TrackedRace} may be garbage
     * collected but may be re-resolved for this column using the race identifier at a later time.
     * 
     * @param fleet
     *            the fleet for which to release its tracked race
     */
    void releaseTrackedRace(Fleet fleet);
    
    
}
