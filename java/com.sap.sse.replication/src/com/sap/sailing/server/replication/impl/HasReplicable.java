package com.sap.sailing.server.replication.impl;

import com.sap.sailing.server.replication.Replicable;

public interface HasReplicable {
    Replicable<?, ?> getReplicable();
}
