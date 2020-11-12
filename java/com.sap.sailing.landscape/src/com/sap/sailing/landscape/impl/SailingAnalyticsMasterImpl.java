package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sse.landscape.Host;

public class SailingAnalyticsMasterImpl<ShardingKey> extends SailingAnalyticsProcessImpl<ShardingKey>
implements SailingAnalyticsMaster<ShardingKey> {
    public SailingAnalyticsMasterImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
    }
}
