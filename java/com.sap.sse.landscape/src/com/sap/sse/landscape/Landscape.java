package com.sap.sse.landscape;

import java.util.Map;

import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;

public interface Landscape<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> {
    /**
     * Tells which scope currently lives where
     */
    Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> getScopes();
}
