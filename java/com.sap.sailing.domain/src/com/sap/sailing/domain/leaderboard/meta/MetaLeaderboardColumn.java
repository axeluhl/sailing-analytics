package com.sap.sailing.domain.leaderboard.meta;

import java.util.Collections;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.SimpleAbstractRaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * All {@link RaceColumnListener} events received from the underlying leaderboard's race columns
 * are forwarded to this object's {@link RaceColumnListener}s.
 * 
 * @author Axel Uhl
 *
 */
public class MetaLeaderboardColumn extends SimpleAbstractRaceColumn implements RaceColumn, RaceColumnListener {
    private static final long serialVersionUID = 3092096133388262955L;
    private final Leaderboard leaderboard;
    private final Fleet metaFleet;
    
    public MetaLeaderboardColumn(Leaderboard leaderboard, Fleet metaFleet) {
        super();
        this.leaderboard = leaderboard;
        this.metaFleet = metaFleet;
        leaderboard.addRaceColumnListener(this);
    }

    @Override
    public RaceLog getRaceLog(Fleet fleet) {
        return null;
    }

    Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Override
    public String getName() {
        return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName();
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        return Collections.singleton(metaFleet);
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        return fleetName.equals(metaFleet.getName()) ? metaFleet : null;
    }

    @Override
    public Fleet getFleetOfCompetitor(Competitor competitor) {
        return metaFleet;
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace race) {
    }

    @Override
    public boolean hasTrackedRaces() {
        return false;
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return null;
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        return null;
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return null;
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
    }

    @Override
    public boolean isMedalRace() {
        return false;
    }

    @Override
    public void releaseTrackedRace(Fleet fleet) {
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        getRaceColumnListeners().notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        getRaceColumnListeners().notifyListenersAboutIsStartsWithZeroScoreChanged(raceColumn, newIsStartsWithZeroScore);
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
        getRaceColumnListeners().notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(raceColumn, firstColumnIsNonDiscardableCarryForward);
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        getRaceColumnListeners().notifyListenersAboutHasSplitFleetContiguousScoringChanged(raceColumn, hasSplitFleetContiguousScoring);
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return getRaceColumnListeners().canAddRaceColumnToContainer(raceColumn);
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnAddedToContainer(raceColumn);
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        getRaceColumnListeners().notifyListenersAboutFactorChanged(raceColumn, oldFactor, newFactor);
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        getRaceColumnListeners().notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName, displayName);
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
        getRaceColumnListeners().notifyListenersAboutResultDiscardingRuleChanged(oldDiscardingRule, newDiscardingRule);
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        getRaceColumnListeners().notifyListenersAboutRaceLogEventAdded(raceColumn, raceLogIdentifier, event);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public RaceLogIdentifier getRaceLogIdentifier(Fleet fleet) {
        return null;
    }

    @Override
    public void removeRaceIdentifier(Fleet fleet) {
    }

    @Override
    public void setMasterDataExportOngoingThreadFlag(boolean flagValue) {
    }

    @Override
    public void setRaceLogInformation(RaceLogStore raceLogStore, RegattaLikeIdentifier regattaLikeParent) {
    }

    @Override
    public void reloadRaceLog(Fleet fleet) {
    }
}
