package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public interface SailingAnalyticsReplica<ShardingKey, MetricsT extends ApplicationProcessMetrics>  extends SailingAnalyticsProcess<ShardingKey, MetricsT>,
ApplicationReplicaProcess<ShardingKey, MetricsT, SailingAnalyticsMaster<ShardingKey, MetricsT>, SailingAnalyticsReplica<ShardingKey, MetricsT>> {

}
