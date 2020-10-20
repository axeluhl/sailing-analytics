package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.orchestration.Procedure;

public class StartMultiServer<ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>> implements
        Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    @Override
    public void run() {
        // TODO Implement Runnable.run(...)
    }

    @Override
    public Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getLandscape() {
        // TODO Implement Procedure<ShardingKey,SailingAnalyticsMetrics,SailingAnalyticsMaster<ShardingKey>,SailingAnalyticsReplica<ShardingKey>>.getLandscape(...)
        return null;
    }
}
