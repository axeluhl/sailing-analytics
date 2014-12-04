package com.sap.sailing.server.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.operationaltransformation.RecordRegattaLogEventOnRegatta;

/**
 * Being a {@link RegattaListener}, this replicator must be added to all {@link Regatta}s managing a {@link RegattaLog}
 * so as to be notified about changes to the regatta log. This largely happens by callbacks to the
 * {@link #regattaLogEventAdded(RaceColumn, RaceLogIdentifier, RaceLogEvent)} method. This object will then use the
 * {@link Replicator} passed to this object's constructor and send a {@link RecordRegattaLogEventOnLeaderboard} or a
 * {@link RecordRegattaLogEventOnRegatta} operation to all replicas.
 * 
 * This class is a modified version of {@link RaceLogReplicator}
 * 
 */
public class RegattaLogReplicator implements RegattaListener {

    private final Replicator service;

    public RegattaLogReplicator(Replicator service) {
        this.service = service;
    }

    @Override
    public void regattaLogEventAdded(final Regatta regatta, final RegattaLogEvent event) {
        RacingEventServiceOperation<?> operation = new RecordRegattaLogEventOnRegatta(regatta.getName(), event);
        service.replicate(operation);
    }

    @Override
    public void raceAdded(Regatta regatta, RaceDefinition race) {
    }

    @Override
    public void raceRemoved(Regatta regatta, RaceDefinition race) {
    }
}
