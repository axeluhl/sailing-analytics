package com.sap.sse.landscape.application.impl;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public class ApplicationReplicaProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends ApplicationProcessImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
implements ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    public ApplicationReplicaProcessImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
    }

    @Override
    public ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getMaster() {
        // TODO Implement ApplicationReplicaProcess<ShardingKey,MetricsT,MasterProcessT,ReplicaProcessT>.getMaster(...)
        return null;
    }
}
