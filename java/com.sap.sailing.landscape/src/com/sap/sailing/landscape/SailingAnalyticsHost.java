package com.sap.sailing.landscape;

import com.sap.sse.landscape.aws.ApplicationProcessHost;

public interface SailingAnalyticsHost<ShardingKey>
        extends ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
}
