package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends
        ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    int getExpeditionUdpPort();
}
