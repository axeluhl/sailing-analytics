package com.sap.sailing.server.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.regattalike.FlexibleLeaderboardAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifierResolver;
import com.sap.sailing.domain.regattalike.RegattaLikeListener;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.RecordRegattaLogEventOnFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.RecordRegattaLogEventOnRegatta;

/**
 * Being a {@link RegattaListener}, this replicator must be added to all {@link RegattaLikeIdentifier regatta-like}
 * objects (these manage a {@link RegattaLog}) so as to be notified about changes to the regatta log. This largely
 * happens by callbacks to the {@link #regattaLogEventAdded} method. This object will then use the {@link Replicator}
 * passed to this object's constructor and send a {@link RecordRegattaLogEventOnRegatta} or a
 * {@link RecordRegattaLogEventOnFlexibleLeaderboard} operation to all replicas.
 * 
 * This class is a modified version of {@link RaceLogReplicatorAndNotifier}
 * 
 */
public class RegattaLogReplicator implements RegattaLikeListener {
    private static final long serialVersionUID = 5266000688811193807L;
    private final Replicator service;

    public RegattaLogReplicator(Replicator service) {
        this.service = service;
    }

    @Override
    public void onRegattaLogEvent(RegattaLikeIdentifier regattaLikeIdentifier, final RegattaLogEvent event) {
        regattaLikeIdentifier.resolve(new RegattaLikeIdentifierResolver() {
            @Override
            public void resolveOnRegattaIdentifier(RegattaAsRegattaLikeIdentifier regattaLikeParent) {
                RacingEventServiceOperation<?> operation = new RecordRegattaLogEventOnRegatta(regattaLikeParent
                        .getName(), event);
                service.replicate(operation);
            }

            @Override
            public void resolveOnFlexibleLeaderboardIdentifier(
                    FlexibleLeaderboardAsRegattaLikeIdentifier regattaLikeParent) {
                RacingEventServiceOperation<?> operation = new RecordRegattaLogEventOnFlexibleLeaderboard(
                        regattaLikeParent.getName(), event);
                service.replicate(operation);
            }
        });
    }
}
