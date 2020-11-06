package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.util.Optional;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * For an {@link ApplicationProcess} creates a "dynamic" load balancer set-up.
 * @author Axel Uhl (D043530)
 */
public class CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
        extends CreateLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>>
        implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    public CreateDynamicLoadBalancerMapping(
            ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> process, String hostname,
            String targetGroupNamePrefix,
            AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape,
            Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException, SftpException {
        super(process, landscape.getNonDNSMappedLoadBalancer(process.getHost().getRegion()), hostname,
                targetGroupNamePrefix, landscape, optionalTimeout);
    }
}
