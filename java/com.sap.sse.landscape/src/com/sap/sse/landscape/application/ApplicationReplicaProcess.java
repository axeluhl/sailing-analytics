package com.sap.sse.landscape.application;

public interface ApplicationReplicaProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends ApplicationProcess<ShardingKey, MetricsT> {
    ApplicationMasterProcess<ShardingKey, MetricsT> getMaster();
}
