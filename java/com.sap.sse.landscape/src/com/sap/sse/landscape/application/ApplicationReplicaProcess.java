package com.sap.sse.landscape.application;

public interface ApplicationReplicaProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT,ReplicaProcessT>>
extends ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getMaster();
}
