package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

@FunctionalInterface
public interface HostSupplier<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>> {
    HostT supply(String instanceId, AwsAvailabilityZone az, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape);
}