package com.sap.sse.landscape.application.impl;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.impl.ProcessImpl;

public abstract class ApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
        extends ProcessImpl<RotatingFileBasedLog, MetricsT>
        implements ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {

    public ApplicationProcessImpl(int port, Host host) {
        super(port, host);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getReplicaSet() {
        // TODO Implement ApplicationProcessImpl.getReplicaSet(...)
        return null;
    }

    @Override
    public Release getRelease() {
        // TODO Implement ApplicationProcessImpl.getRelease(...)
        return null;
    }

    @Override
    public boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout) {
        // TODO Implement ApplicationProcessImpl.tryCleanShutdown(...)
        return false;
    }
    
    @Override
    public int getTelnetPortToOSGiConsole() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getTelnetPortToOSGiConsole(...)
        return 0;
    }
    
    @Override
    public String getServerDirectory() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getServerDirectory(...)
        return null;
    }

    @Override
    public String getServerName() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getServerName(...)
        return null;
    }

    @Override
    public String getEnvSh() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getEnvSh(...)
        return null;
    }
}
