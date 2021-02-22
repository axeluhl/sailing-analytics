package com.sap.sailing.landscape;

import com.sap.sse.landscape.aws.ApplicationProcessHost;

public interface SailingAnalyticsHost<ShardingKey, ProcessT extends SailingAnalyticsProcess<ShardingKey>>
        extends ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    String SAILING_ANALYTICS_APPLICATION_HOST_TAG = "sailing-analytics-server";
}
