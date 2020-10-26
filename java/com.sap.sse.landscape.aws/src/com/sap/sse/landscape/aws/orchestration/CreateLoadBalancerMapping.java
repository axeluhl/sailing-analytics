package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;

/**
 * For an {@link ApplicationProcess} creates a set of rules in an {@link ApplicationLoadBalancer} which drives traffic
 * to one of the two {@link TargetGroup}s that this procedure will also create. One will take the traffic for the single
 * "master" node; the other will take the traffic for all public-facing nodes which by default in the minimal
 * application server replica set configuration will be the single master node. As the number of replicas grows, the
 * master may choose to only serve the writing requests and be removed from the public-facing target group again.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class CreateLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
        extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    public CreateLoadBalancerMapping(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super(landscape);
    }
}
