package com.sap.sse.landscape.aws.orchestration;

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
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
extends CreateLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>
implements Procedure<ShardingKey, MetricsT, ProcessT> {
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT, HostT>,
    T extends CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationProcess<ShardingKey, MetricsT, MasterProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.Builder<BuilderT, T, ShardingKey, MetricsT, MasterProcessT, HostT> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.BuilderImpl<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT>
    implements Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT> {
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
                Region region, String hostname, AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape) throws InterruptedException {
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
                AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape,
                ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancer, String domainName) {
            final String hostname = "*." + domainName;
            landscape.setDNSRecordToApplicationLoadBalancer(landscape.getDNSHostedZoneId(domainName), hostname, loadBalancer);
        }

        @Override
        public CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT> build() throws Exception {
            return new CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>(this);
        }
    }

    public static <MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>,
    BuilderT extends Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT>,
    ShardingKey>
    Builder<BuilderT, CreateDynamicLoadBalancerMapping<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT, HostT>();
    }

    protected CreateDynamicLoadBalancerMapping(BuilderImpl<?, ShardingKey, MetricsT, ProcessT, HostT> builder) throws Exception {
        super(builder);
    }
}
