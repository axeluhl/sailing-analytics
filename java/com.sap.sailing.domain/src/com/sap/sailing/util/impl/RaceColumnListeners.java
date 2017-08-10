package com.sap.sailing.util.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Manages a set of {@link RaceColumnListener}s with the usual add/remove and notification logic. It is {@link Serializable}
 * and serializes only those listeners that are not {@link RaceColumnListener#isTransient() transient}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceColumnListeners implements Serializable {
    private static final long serialVersionUID = -7278209809901582157L;
    private transient Set<RaceColumnListener> raceColumnListeners;

    public RaceColumnListeners() {
        raceColumnListeners = new HashSet<>();
    }
    
    @SuppressWarnings("unchecked") // need to cast to a typed generic
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        raceColumnListeners = (Set<RaceColumnListener>) ois.readObject();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        final Set<RaceColumnListener> setToWrite;
        synchronized (raceColumnListeners) {
            setToWrite = new HashSet<>();
            for (RaceColumnListener listener : raceColumnListeners) {
                if (!listener.isTransient()) {
                    setToWrite.add(listener);
                }
            }
        }
        oos.writeObject(setToWrite);
    }
    
    public void addRaceColumnListener(RaceColumnListener listener) {
        synchronized (raceColumnListeners) {
            raceColumnListeners.add(listener);
        }
    }

    public void removeRaceColumnListener(RaceColumnListener listener) {
        synchronized (raceColumnListeners) {
            raceColumnListeners.remove(listener);
        }
    }
    
    public void notifyListenersAboutTrackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.trackedRaceLinked(raceColumn, fleet, trackedRace);
        }
    }

    public void notifyListenersAboutTrackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
        }
    }

    public void notifyListenersAboutFactorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.factorChanged(raceColumn, oldFactor, newFactor);
        }
    }

    public void notifyListenersAboutIsMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.isMedalRaceChanged(raceColumn, newIsMedalRace);
        }
    }

    public void notifyListenersAboutIsFleetsCanRunInParallelChanged(RaceColumn raceColumn, boolean newIsFleetsCanRunInParallel) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.isFleetsCanRunInParallelChanged(raceColumn, newIsFleetsCanRunInParallel);
        }
    }

    public void notifyListenersAboutIsStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.isStartsWithZeroScoreChanged(raceColumn, newIsStartsWithZeroScore);
        }
    }

    public void notifyListenersAboutHasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.hasSplitFleetContiguousScoringChanged(raceColumn, hasSplitFleetContiguousScoring);
        }
    }

    public void notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.isFirstColumnIsNonDiscardableCarryForwardChanged(raceColumn, firstColumnIsNonDiscardableCarryForward);
        }
    }

    public void notifyListenersAboutRaceColumnAddedToContainer(RaceColumn raceColumn) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.raceColumnAddedToContainer(raceColumn);
        }
    }

    public void notifyListenersAboutRaceColumnRemovedFromContainer(RaceColumn raceColumn) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.raceColumnRemovedFromContainer(raceColumn);
        }
    }

    public void notifyListenersAboutRaceColumnMoved(RaceColumn raceColumn, int newIndex) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.raceColumnMoved(raceColumn, newIndex);
        }
    }

    public void notifyListenersAboutRaceColumnNameChanged(RaceColumn raceColumn, String oldName, String newName) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.raceColumnNameChanged(raceColumn, oldName, newName);
        }
    }

    private Set<RaceColumnListener> getRaceColumnListeners() {
        synchronized (raceColumnListeners) {
            return new HashSet<RaceColumnListener>(raceColumnListeners);
        }
    }

    public boolean canAddRaceColumnToContainer(RaceColumn columnToAdd) {
        boolean result = true;
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            result = listener.canAddRaceColumnToContainer(columnToAdd);
            if (!result) {
                break;
            }
        }
        return result;
    }

    public void notifyListenersAboutCompetitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.competitorDisplayNameChanged(competitor, oldDisplayName, displayName);
        }
    }

    public void notifyListenersAboutResultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule,
            ResultDiscardingRule newDiscardingRule) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.resultDiscardingRuleChanged(oldDiscardingRule, newDiscardingRule);
        }
    }

    public void notifyListenersAboutRaceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.raceLogEventAdded(raceColumn, raceLogIdentifier, event);
        }
    }

    public void notifyListenersAboutRegattaLogEventAdded(RegattaLogEvent event) {
        for (RaceColumnListener listener : getRaceColumnListeners()) {
            listener.regattaLogEventAdded(event);
        }
    }
}
