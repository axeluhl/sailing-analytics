package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public interface SailingAnalyticsMaster<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends SailingAnalyticsProcess<ShardingKey, MetricsT>,
ApplicationMasterProcess<ShardingKey, MetricsT, SailingAnalyticsMaster<ShardingKey, MetricsT>, SailingAnalyticsReplica<ShardingKey, MetricsT>> {

}
