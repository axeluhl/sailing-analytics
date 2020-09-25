package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public interface SailingAnalyticsReplica<ShardingKey, MetricsT extends ApplicationProcessMetrics>  extends SailingAnalyticsProcess<ShardingKey, MetricsT>  {

}
