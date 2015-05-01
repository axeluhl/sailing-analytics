package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Delegates all listener operations to a default action which is implemented here to do nothing. This way, subclasses
 * can easily provide a default action to be executed for all callback methods except for maybe a few which then need to
 * be explicitly overridden. If not most methods need to perform the same action then instead of implementing a default
 * action subclasses should rather override the {@link RaceColumnListener} operations individually.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public abstract class AbstractRaceColumnListener implements RaceColumnListener{

    private static final long serialVersionUID = -6210815580399401162L;

    /**
     * This implementation of the default action to which all other operations delegate does nothing. Subclasses can
     * choose to override this method to provide this as the default behavior for all other methods and then still
     * override individual {@link RaceColumnListener} methods, or they can leave the default action empty and just
     * override {@link RaceColumnListener} methods.
     */
    protected void defaultAction() {
        // Does nothing
    }
    
    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        defaultAction(); 
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        defaultAction(); 
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        defaultAction(); 
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        defaultAction(); 
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn,
            boolean firstColumnIsNonDiscardableCarryForward) {
        defaultAction(); 
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        defaultAction(); 
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return false;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        defaultAction(); 
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        defaultAction(); 
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        defaultAction(); 
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        defaultAction(); 
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        defaultAction(); 
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule,
            ResultDiscardingRule newDiscardingRule) {
        defaultAction(); 
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        defaultAction(); 
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
