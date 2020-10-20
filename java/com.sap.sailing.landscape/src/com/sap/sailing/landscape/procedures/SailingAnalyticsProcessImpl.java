package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationReplicaSet;

public class SailingAnalyticsProcessImpl<ShardingKey> implements SailingAnalyticsProcess<ShardingKey> {

    @Override
    public ApplicationReplicaSet<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getReplicaSet() {
        // TODO Implement ApplicationProcess<ShardingKey,SailingAnalyticsMetrics,SailingAnalyticsMaster<ShardingKey>,SailingAnalyticsReplica<ShardingKey>>.getReplicaSet(...)
        return null;
    }

    @Override
    public boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout) {
        // TODO Implement ApplicationProcess<ShardingKey,SailingAnalyticsMetrics,SailingAnalyticsMaster<ShardingKey>,SailingAnalyticsReplica<ShardingKey>>.tryCleanShutdown(...)
        return false;
    }

    @Override
    public int getPort() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.getPort(...)
        return 0;
    }

    @Override
    public Host getHost() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.getHost(...)
        return null;
    }

    @Override
    public RotatingFileBasedLog getLog() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.getLog(...)
        return null;
    }

    @Override
    public SailingAnalyticsMetrics getMetrics() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.getMetrics(...)
        return null;
    }

    @Override
    public boolean isReady() {
        // TODO Implement Process<RotatingFileBasedLog,SailingAnalyticsMetrics>.isReady(...)
        return false;
    }

    @Override
    public int getTelnetPortToOSGiConsole() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getTelnetPortToOSGiConsole(...)
        return 0;
    }

    @Override
    public int getExpeditionUdpPort() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getExpeditionUdpPort(...)
        return 0;
    }

    @Override
    public String getServerDirectory() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getServerDirectory(...)
        return null;
    }

    @Override
    public String getEnvSh() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getEnvSh(...)
        return null;
    }

}
