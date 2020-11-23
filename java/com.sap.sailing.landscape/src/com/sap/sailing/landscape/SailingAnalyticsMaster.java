package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationMasterProcess;

public interface SailingAnalyticsMaster<ShardingKey> extends SailingAnalyticsProcess<ShardingKey>,
ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {

}
