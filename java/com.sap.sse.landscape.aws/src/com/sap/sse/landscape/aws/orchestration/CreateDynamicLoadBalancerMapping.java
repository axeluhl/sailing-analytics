package com.sap.sse.landscape.aws.orchestration;

import java.util.concurrent.ExecutionException;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
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
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends CreateLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>
implements Procedure<ShardingKey> {
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT>,
    T extends CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationProcess<ShardingKey, MetricsT, MasterProcessT>>
    extends CreateLoadBalancerMapping.Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends CreateLoadBalancerMapping.BuilderImpl<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {
        @Override
        public ApplicationLoadBalancer<ShardingKey> getLoadBalancerUsed() throws InterruptedException, ExecutionException {
            final ApplicationLoadBalancer<ShardingKey> result;
            if (super.getLoadBalancerUsed() != null) {
                result = super.getLoadBalancerUsed();
            } else {
                result = getOrCreateNonDNSMappedLoadBalancer(getProcess().getHost().getRegion(), getHostname(), getLandscape());
            }
            return result;
        }

        private ApplicationLoadBalancer<ShardingKey> getOrCreateNonDNSMappedLoadBalancer(
                Region region, String hostname, AwsLandscape<ShardingKey> landscape) throws InterruptedException, ExecutionException {
            final String domainName = AwsLandscape.getHostedZoneName(hostname);
            final ApplicationLoadBalancer<ShardingKey> existingLoadBalancer = landscape.getNonDNSMappedLoadBalancer(region, domainName);
            final ApplicationLoadBalancer<ShardingKey> result;
            if (existingLoadBalancer != null) {
                result = existingLoadBalancer;
            } else {
                result = landscape.createNonDNSMappedLoadBalancer(region, domainName, getSecurityGroupForVpc());
                waitUntilLoadBalancerProvisioned(landscape, result);
                createWildcardRoute53Mapping(landscape, result, domainName);
            }
            return result;
        }
        
        private void createWildcardRoute53Mapping(
                AwsLandscape<ShardingKey> landscape,
                ApplicationLoadBalancer<ShardingKey> loadBalancer, String domainName) {
            final String hostname = "*." + domainName;
            landscape.setDNSRecordToApplicationLoadBalancer(landscape.getDNSHostedZoneId(domainName), hostname, loadBalancer, /* force */ false);
        }

        @Override
        public CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            return new CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>(this);
        }
    }

    public static <MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey>,
    BuilderT extends Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>,
    ShardingKey>
    Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }

    protected CreateDynamicLoadBalancerMapping(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
    }
}
