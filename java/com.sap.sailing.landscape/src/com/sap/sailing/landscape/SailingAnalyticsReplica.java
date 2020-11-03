package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public interface SailingAnalyticsReplica<ShardingKey>  extends SailingAnalyticsProcess<ShardingKey>,
ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {

}
