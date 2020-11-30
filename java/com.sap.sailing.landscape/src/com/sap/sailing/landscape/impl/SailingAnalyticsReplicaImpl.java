package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.ApplicationMasterProcess;

public class SailingAnalyticsReplicaImpl<ShardingKey> extends SailingAnalyticsProcessImpl<ShardingKey>
        implements SailingAnalyticsReplica<ShardingKey> {
    public SailingAnalyticsReplicaImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
    }

    @Override
    public ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getMaster() {
        // TODO Implement ApplicationReplicaProcess<ShardingKey,SailingAnalyticsMetrics,SailingAnalyticsMaster<ShardingKey>,SailingAnalyticsReplica<ShardingKey>>.getMaster(...)
        return null;
    }
}
