package com.sap.sse.landscape.application;

public interface ApplicationMasterProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> extends ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
}
