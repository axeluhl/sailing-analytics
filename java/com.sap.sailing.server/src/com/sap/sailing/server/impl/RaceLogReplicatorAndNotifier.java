package com.sap.sailing.server.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.regattalike.FlexibleLeaderboardAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifierResolver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.RecordRaceLogEventOnLeaderboard;
import com.sap.sailing.server.operationaltransformation.RecordRaceLogEventOnRegatta;

/**
 * Being a {@link RaceColumnListener}, this replicator and notifier must be added to all {@link RaceColumn}s managing a
 * {@link RaceLog} so as to be notified about changes to the race log. This largely happens by callbacks to the
 * {@link #raceLogEventAdded(RaceColumn, RaceLogIdentifier, RaceLogEvent)} method. This object will then use the
 * {@link Replicator} passed to this object's constructor and send a {@link RecordRaceLogEventOnLeaderboard} or a
 * {@link RecordRaceLogEventOnRegatta} operation to all replicas.<p>
 * 
 * Since all {@link RaceColumn}s can manage a {@link RaceLog}, this object is registered on all relevant
 * {@link RaceColumn}s. This also lets this object conveniently being informed about all other events that
 * a {@link RaceColumnListener} is notified about, some of which contain interesting information regarding
 * the notifying of users interested in certain types of changes. For example, when a {@link TrackedRace}
 * {@link #trackedRaceLinked(RaceColumn, Fleet, TrackedRace) is linked} to a race column, this may be of
 * interest to some users.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceLogReplicatorAndNotifier implements RaceColumnListenerWithDefaultAction {
    private static final long serialVersionUID = 7190510926643574068L;
    
    private final Replicator service;
    
    public RaceLogReplicatorAndNotifier(Replicator service) {
        this.service = service;
    }

    @Override
    public void raceLogEventAdded(final RaceColumn raceColumn, final RaceLogIdentifier identifier, final RaceLogEvent event) {
        identifier.getRegattaLikeParent().resolve(new RegattaLikeIdentifierResolver() {
            @Override
            public void resolveOnRegattaIdentifier(RegattaAsRegattaLikeIdentifier regattaLikeParent) {
                RacingEventServiceOperation<?> operation = new RecordRaceLogEventOnRegatta(
                        regattaLikeParent.getName(),
                        raceColumn.getName(), 
                        identifier.getFleetName(), 
                        event);
                service.replicate(operation);
            }
            
            @Override
            public void resolveOnFlexibleLeaderboardIdentifier(FlexibleLeaderboardAsRegattaLikeIdentifier regattaLikeParent) {
                RacingEventServiceOperation<?> operation = new RecordRaceLogEventOnLeaderboard(
                        regattaLikeParent.getName(), 
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
    
    /**
     * The default action for this {@link RaceColumnListener} is to do nothing.
     */
    @Override
    public void defaultAction() {
    }

    
}
