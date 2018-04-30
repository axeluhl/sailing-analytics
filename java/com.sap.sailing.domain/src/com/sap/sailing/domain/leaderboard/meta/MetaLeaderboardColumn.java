package com.sap.sailing.domain.leaderboard.meta;

import java.util.Collections;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardChangeListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.SimpleAbstractRaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util.Pair;

/**
 * All {@link RaceColumnListener} events received from the underlying leaderboard's race columns
 * are forwarded to this object's {@link RaceColumnListener}s.
 * 
 * @author Axel Uhl
 *
 */
public class MetaLeaderboardColumn extends SimpleAbstractRaceColumn implements RaceColumn, RaceColumnListener, LeaderboardChangeListener {
    private static final long serialVersionUID = 3092096133388262955L;
    private final Leaderboard leaderboard;
    private final Fleet metaFleet;
    
    public MetaLeaderboardColumn(Leaderboard leaderboard, Fleet metaFleet) {
        super();
        this.leaderboard = leaderboard;
        this.metaFleet = metaFleet;
        leaderboard.addRaceColumnListener(this);
        leaderboard.addLeaderboardChangeListener(this);
    }

    @Override
    public RaceLog getRaceLog(Fleet fleet) {
        return null;
    }

    public Leaderboard getLeaderboard() {
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
    public void isFleetsCanRunInParallelChanged(RaceColumn raceColumn, boolean newIsFleetsCanRunInParallel) {
        getRaceColumnListeners().notifyListenersAboutIsFleetsCanRunInParallelChanged(raceColumn, newIsFleetsCanRunInParallel);
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
    public void raceColumnNameChanged(RaceColumn raceColumn, String oldName, String newName) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnNameChanged(raceColumn, oldName, newName);
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

    @Override
    public RaceExecutionOrderProvider getRaceExecutionOrderProvider() {
        return null;
    }

    @Override
    public Iterable<Competitor> getAllCompetitors() {
        return getAllCompetitorsWithRaceDefinitionsConsidered().getB();
    }

    @Override
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        return leaderboard.getAllCompetitorsWithRaceDefinitionsConsidered();
    }
    
    @Override
    public Iterable<Competitor> getAllCompetitors(Fleet fleet) {
        final Iterable<Competitor> result;
        if (fleet == metaFleet) {
            result = leaderboard.getAllCompetitors();
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    @Override
    public Map<Competitor, Boat> getAllCompetitorsAndTheirBoats() {
        // TODO bug2822: What should we do here? Returning a boat makes only sense when the competitors keep their boats through all regattas
        return Collections.emptyMap();
    }

    @Override
    public Map<Competitor, Boat> getAllCompetitorsAndTheirBoats(Fleet fleet) {
        // TODO bug2822: What should we do here? Returning a boat makes only sense when the competitors keep their boats through all regattas 
        return Collections.emptyMap();
    }

    @Override
    public RegattaLog getRegattaLog() {
        return null;
    }

    @Override
    public Iterable<Mark> getCourseMarks() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<Mark> getCourseMarks(Fleet fleet) {
        return Collections.emptySet();
    }

    @Override
    public Iterable<Mark> getAvailableMarks() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<Mark> getAvailableMarks(Fleet fleet) {
        return Collections.emptySet();
    }

    @Override
    public void registerCompetitor(CompetitorWithBoat competitorWithBoat, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void registerCompetitors(Iterable<CompetitorWithBoat> competitorsWithBoat, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void registerCompetitor(Competitor competitor, Boat boat, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void registerCompetitors(Map<Competitor, Boat> competitorsAndBoats, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void deregisterCompetitor(Competitor competitors, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void deregisterCompetitors(Iterable<? extends Competitor> competitors, Fleet fleet)
            throws CompetitorRegistrationOnRaceLogDisabledException {
        throw new CompetitorRegistrationOnRaceLogDisabledException();
    }

    @Override
    public void enableCompetitorRegistrationOnRaceLog(Fleet fleetByName) {
    }

    @Override
    public void disableCompetitorRegistrationOnRaceLog(Fleet fleetByName) {
    }

    /**
     * When the leaderboard name changes, notify this to this object's {@link RaceColumnListener}s as a
     * change of this race column's name, but only if no {@link Leaderboard#getDisplayName() display name}
     * is set because that would take precedence over the regular name.
     */
    @Override
    public void nameChanged(String oldName, String newName) {
        if (leaderboard.getDisplayName() == null) {
            getRaceColumnListeners().notifyListenersAboutRaceColumnNameChanged(this, oldName, newName);
        }
    }

    /**
     * When the leaderboard display name changes, notify this to this object's {@link RaceColumnListener}s as a
     * change of this race column's name
     */
    @Override
    public void displayNameChanged(String oldDisplayName, String newDisplayName) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnNameChanged(this, oldDisplayName, newDisplayName);
    }
}
