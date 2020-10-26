package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * For an {@link ApplicationProcess} creates a "dynamic" load balancer set-up.
 * @author Axel Uhl (D043530)
 */
public class CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
        extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
        implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    public CreateDynamicLoadBalancerMapping(
            Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super(landscape);
    }

    @Override
    public void run() {
        // TODO Implement Runnable.run(...)

    }
}
