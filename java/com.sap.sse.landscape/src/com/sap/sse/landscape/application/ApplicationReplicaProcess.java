package com.sap.sse.landscape.application;

public interface ApplicationReplicaProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT,ReplicaProcessT>>
extends ApplicationProcess<ShardingKey, MetricsT> {
    /**
     * TODO Problem: a replica will survive a change in master as long as there is a target group to which the replica
     * can fire its master-bound requests, usually mapped there by a load balancer. But ApplicationMasterProcess
     * references a Host object which in turn represents a single AWS EC2 Instance. Does this mean that this
     * method would have to explore the landscape dynamically to find the master? Or is this meant to go by
     * the configuration in the replica which consists mainly of the hostname/port combination for sending HTTP
     * requests to the master?
     */
    MasterProcessT getMaster();
}
