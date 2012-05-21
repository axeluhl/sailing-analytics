package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A column in a {@link Leaderboard} that represents the data of a race. Over the life time of this object it can be
 * assigned a {@link TrackedRace} which then acts as a data provider to this column. If no tracked race has been
 * assigned, the scores reported by this column will all default to zero.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface RaceColumn extends Named {
    /**
     * This does also update the {@link #getRaceIdentifier() race identifier} by setting it to <code>null</code> if <code>race</code>
     * is <code>null</code>, and <code>race.getRaceIdentifier()</code> otherwise.
     */
    void setTrackedRace(TrackedRace race);

    /**
     * @return <code>null</code> if this column does not currently have a tracked race associated; otherwise the tracked
     *         race from where all information relevant to this column can be obtained. See also
     *         {@link #getRaceIdentifier()}.
     */
    TrackedRace getTrackedRace();
    
    /**
     * If this column ever was assigned to a tracked race, that race's identifier can be obtained using this method;
     * otherwise, <code>null</code> is returned. 
     */
    RaceIdentifier getRaceIdentifier();
    
    /**
     * Records that this leaderboard column is to be associated with the race identified by <code>raceIdentifier</code>.
     * This does not automatically load the tracked race, but the information may be used to re-associate a tracked
     * race with this column based on its {@link TrackedRace#getRaceIdentifier() race identifier}.
     */
    void setRaceIdentifier(RaceIdentifier raceIdentifier);
    
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
     * Releases the {@link TrackedRace} previously set by {@link #setTrackedRace(TrackedRace)} but leaves the
     * {@link #getRaceIdentifier() race identifier} untouched. Therefore, the {@link TrackedRace} may be garbage
     * collected but may be re-resolved for this column using the race identifier at a later time.
     */
    void releaseTrackedRace();
    
    
}
