package com.sap.sailing.domain.base;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface RaceColumnListener extends Serializable {
    void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
    
    void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
    
    void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace);

    void isFleetsCanRunInParallelChanged(RaceColumn raceColumn, boolean newIsFleetsCanRunInParallel);

    void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore);
    
    void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward);

    void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring);

    default boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return true;
    }
    
    void raceColumnAddedToContainer(RaceColumn raceColumn);
    
    void raceColumnRemovedFromContainer(RaceColumn raceColumn);
    
    void raceColumnMoved(RaceColumn raceColumn, int newIndex);
    
    void raceColumnNameChanged(RaceColumn raceColumn, String oldName, String newName);
    
    void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor);

    void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName);

    void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule);

    void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event);
    
    default void regattaLogEventAdded(RegattaLogEvent event) {};

    /**
     * A listener can use this to specify that it must not be serialized together with other listeners.
     * Background: the collections holding race column listeners are usually non-transient. This will cause
     * the set of listeners to be serialized. However, for some listeners, serialization does not make sense.
     * Instead of making the entire listener collection transient, with this method it is possible that
     * individual listeners remove themselves from the serialization output.<p>
     * 
     * Note that it is the responsibility of the class holding a collection of objects of this type to
     * exclude listeners from the serialization that return <code>true</code> from this method.
     */
    boolean isTransient();

}
