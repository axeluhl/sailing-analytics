package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public interface SailingAnalyticsProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends
        ApplicationProcess<ShardingKey, MetricsT, SailingAnalyticsMaster<ShardingKey, MetricsT>, SailingAnalyticsReplica<ShardingKey, MetricsT>> {
    int getTelnetPort();
    int getExpeditionUdpPort();
}
