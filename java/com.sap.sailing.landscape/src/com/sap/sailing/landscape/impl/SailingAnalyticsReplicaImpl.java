package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Host;

public class SailingAnalyticsReplicaImpl<ShardingKey> extends SailingAnalyticsProcessImpl<ShardingKey>
        implements SailingAnalyticsReplica<ShardingKey> {
    public SailingAnalyticsReplicaImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
    }

    @Override
    public SailingAnalyticsMaster<ShardingKey> getMaster() {
        // TODO Implement ApplicationReplicaProcess<ShardingKey,SailingAnalyticsMetrics,SailingAnalyticsMaster<ShardingKey>,SailingAnalyticsReplica<ShardingKey>>.getMaster(...)
        return null;
    }
}
