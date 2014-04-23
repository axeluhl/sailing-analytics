package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Base implementing class with method stubs.
 * @author Fredrik Teschke
 *
 */
public abstract class BaseRaceColumnListener implements RaceColumnListener {
    private static final long serialVersionUID = 5390243590559755813L;

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn,
            boolean firstColumnIsNonDiscardableCarryForward) {
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return false;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule,
            ResultDiscardingRule newDiscardingRule) {
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
    }

    @Override
    public boolean isTransient() {
        return true;
    }

}
