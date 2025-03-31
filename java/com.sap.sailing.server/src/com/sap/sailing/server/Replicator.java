package com.sap.sailing.server;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public interface Replicator extends com.sap.sse.replication.Replicator<RacingEventService, RacingEventServiceOperation<?>> {
}
