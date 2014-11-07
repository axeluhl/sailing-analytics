package com.sap.sailing.server.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplateResolver;
import com.sap.sailing.domain.racelog.impl.RaceLogOnLeaderboardIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.operationaltransformation.RecordRaceLogEventOnLeaderboard;
import com.sap.sailing.server.operationaltransformation.RecordRaceLogEventOnRegatta;

/**
 * Being a {@link RaceColumnListener}, this replicator must be added to all {@link RaceColumn}s managing a
 * {@link RaceLog} so as to be notified about changes to the race log. This largely happens by callbacks to the
 * {@link #raceLogEventAdded(RaceColumn, RaceLogIdentifier, RaceLogEvent)} method. This object will then use the
 * {@link Replicator} passed to this object's constructor and send a {@link RecordRaceLogEventOnLeaderboard} or a
 * {@link RecordRaceLogEventOnRegatta} operation to all replicas.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceLogReplicator implements RaceColumnListener {
    private static final long serialVersionUID = 7190510926643574068L;
    
    private final Replicator service;
    
    public RaceLogReplicator(Replicator service) {
        this.service = service;
    }

    @Override
    public void raceLogEventAdded(final RaceColumn raceColumn, final RaceLogIdentifier identifier, final RaceLogEvent event) {
        identifier.getTemplate().resolve(new RaceLogIdentifierTemplateResolver() {
            @Override
            public void resolveOnRegattaIdentifierAndReplicate(RaceLogOnRegattaIdentifier identifierTemplate) {
                RacingEventServiceOperation<?> operation = new RecordRaceLogEventOnRegatta(
                        identifierTemplate.getParentObjectName(), 
                        raceColumn.getName(), 
                        identifier.getFleetName(), 
                        event);
                service.replicate(operation);
            }
            
            @Override
            public void resolveOnLeaderboardIdentifierAndReplicate(RaceLogOnLeaderboardIdentifier identifierTemplate) {
                RacingEventServiceOperation<?> operation = new RecordRaceLogEventOnLeaderboard(
                        identifierTemplate.getParentObjectName(), 
                        raceColumn.getName(), 
                        identifier.getFleetName(), 
                        event);
                service.replicate(operation);
            }
        });
    }

    @Override
    public boolean isTransient() {
        return true;
    }
    
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
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return true;
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
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
    }

    
}
