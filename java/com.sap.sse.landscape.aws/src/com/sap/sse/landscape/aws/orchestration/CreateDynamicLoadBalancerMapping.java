package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * For an {@link ApplicationProcess} creates a "dynamic" load balancer set-up.
 * 
 * @author Axel Uhl (D043530)
 */
public class CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
extends CreateLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> build() throws JSchException, IOException, InterruptedException, SftpException;
    }
    
    protected static class BuilderImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
    implements Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        @Override
        public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() throws InterruptedException {
            final ApplicationLoadBalancer<ShardingKey, MetricsT> result;
            if (super.getLoadBalancerUsed() != null) {
                result = super.getLoadBalancerUsed();
            } else {
                result = getOrCreateNonDNSMappedLoadBalancer(getProcess().getHost().getRegion(), getHostname(), getLandscape());
            }
            return result;
        }

        private ApplicationLoadBalancer<ShardingKey, MetricsT> getOrCreateNonDNSMappedLoadBalancer(
                Region region, String hostname, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) throws InterruptedException {
            final String domainName = getHostedZoneName(hostname);
            final ApplicationLoadBalancer<ShardingKey, MetricsT> existingLoadBalancer = landscape.getNonDNSMappedLoadBalancer(region, domainName);
            final ApplicationLoadBalancer<ShardingKey, MetricsT> result;
            if (existingLoadBalancer != null) {
                result = existingLoadBalancer;
            } else {
                result = landscape.createNonDNSMappedLoadBalancer(region, domainName);
                waitUntilLoadBalancerProvisioned(landscape, result);
                createWildcardRoute53Mapping(landscape, result, domainName);
            }
            return result;
        }
        
        private void createWildcardRoute53Mapping(
                AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape,
                ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancer, String domainName) {
            final String hostname = "*." + domainName;
            landscape.setDNSRecordToApplicationLoadBalancer(landscape.getDNSHostedZoneId(domainName), hostname, loadBalancer);
        }

        @Override
        public CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> build() throws JSchException, IOException, InterruptedException, SftpException {
            return new CreateDynamicLoadBalancerMapping<>(this);
        }
    }

    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder() {
        return new BuilderImpl<>();
    }

    protected CreateDynamicLoadBalancerMapping(BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) throws JSchException, IOException, InterruptedException, SftpException {
        super(builder);
    }
}
