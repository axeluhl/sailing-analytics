package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;

public class SailingAnalyticsProcessImpl<ShardingKey> extends ApplicationProcessImpl<ShardingKey, SailingAnalyticsMetrics,
SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> implements SailingAnalyticsProcess<ShardingKey> {
    
    public SailingAnalyticsProcessImpl(int port, Host host) {
        super(port, host, "/home/sailing/servers/server");
    }

    @Override
    public boolean isAlive() {
        // TODO Implement SailingAnalyticsProcessImpl.isAlive(...)
        return super.isAlive();
    }
    
    @Override
    public boolean isReady() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.isReady(...)
        return false;
    }

    @Override
    public int getExpeditionUdpPort() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getExpeditionUdpPort(...)
        return 0;
    }
}
