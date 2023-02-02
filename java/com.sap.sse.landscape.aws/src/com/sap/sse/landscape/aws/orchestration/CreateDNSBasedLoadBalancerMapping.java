package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.IntStream;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * For an {@link ApplicationProcess} creates a DNS-based load balancer set-up. This entails setting up a Route53 DNS entry
 * for the hostname, pointing to the load balancer's {@link ApplicationLoadBalancer#getDNSName() DNS name} as a CNAME record.
 * Furthermore, this procedure has logic to provision a new ALB in case no other DNS-mapped ALB has sufficient rule capacity
 * (see {@link #MAX_RULES_PER_ALB}) to accommodate the {@link #NUMBER_OF_RULES_PER_REPLICA_SET} additional rules required.
 * The naming scheme for those DNS-mapped ALBs is expected to follow the pattern {@link #DNS_MAPPED_ALB_NAME_PREFIX}{@code [0-9]*} which
 * means that names such as "DNSMapped-4" and "DNSMapped-7" are valid names.<p>
 * 
 * By default, this procedure will not force a DNS update but fail with an {@link IllegalStateException} in case a competing
 * DNS record is already set, pointing to a different load balancer or some other value. Only when using {@link Builder#forceDNSUpdate(boolean)},
 * forcing the DNS record to be changed can be requested. Use with caution.
 * 
 * @author Axel Uhl (D043530)
 */
public class CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends CreateLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>
implements Procedure<ShardingKey> {
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends CreateLoadBalancerMapping.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        /**
         * @param forceDNSUpdate
         *            when {@code true}, the DNS record will be set to the new value even if an equal-named record for
         *            that name already exists; use with caution!
         */
        BuilderT forceDNSUpdate(boolean forceDNSUpdate);
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends CreateLoadBalancerMapping.BuilderImpl<BuilderT, CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>
    implements Builder<BuilderT, CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {
        private static final Logger logger = Logger.getLogger(BuilderImpl.class.getName());
        private boolean forceDNSUpdate;
        
        @Override
        public BuilderT forceDNSUpdate(boolean forceDNSUpdate) {
            this.forceDNSUpdate = forceDNSUpdate;
            return self();
        }

        @Override
        public ApplicationLoadBalancer<ShardingKey> getLoadBalancerUsed() throws InterruptedException, ExecutionException {
            final ApplicationLoadBalancer<ShardingKey> result;
            if (super.getLoadBalancerUsed() != null) {
                result = super.getLoadBalancerUsed();
            } else {
                result = getOrCreateDNSMappedLoadBalancer(getLandscape(), getProcess().getHost().getRegion());
            }
            return result;
        }

        /**
         * Finds or creates a {@link ApplicationLoadBalander} load balancer in the {@code region} that is DNS-mapped and still has
         * at least the length of {@link #createRules()} additional rules available.
         */
        private ApplicationLoadBalancer<ShardingKey> getOrCreateDNSMappedLoadBalancer(
                AwsLandscape<ShardingKey> landscape, Region region) throws InterruptedException, ExecutionException {
            ApplicationLoadBalancer<ShardingKey> result = null;
            final Set<String> loadBalancerNames = new HashSet<>();
            for (final ApplicationLoadBalancer<ShardingKey> loadBalancer : landscape.getLoadBalancers(region)) {
                if (ApplicationLoadBalancer.ALB_NAME_PATTERN.matcher(loadBalancer.getName()).matches()) {
                    loadBalancerNames.add(loadBalancer.getName());
                    if (Util.size(loadBalancer.getRules()) <= MAX_RULES_PER_ALB - NUMBER_OF_RULES_PER_REPLICA_SET) {
                        result = loadBalancer;
                        break;
                    }
                }
            }
            if (result == null) {
                String newLoadBalancerName = getAvailableDNSMappedAlbName(loadBalancerNames);
                logger.info("Creating DNS-mapped application load balancer "+newLoadBalancerName);
                result = landscape.createLoadBalancer(newLoadBalancerName, region);
                waitUntilLoadBalancerProvisioned(landscape, result);
            }
            return result;
        }

        /**
         * Picks a new load balancer name following the pattern {@link #ApplicationLoadBalancer.DNS_MAPPED_ALB_NAME_PREFIX}{@code [0-9]+} that is not
         * part of {@code loadBalancerNames} and has the least number.
         */
        private String getAvailableDNSMappedAlbName(Set<String> loadBalancerNames) {
            final Set<Integer> numbersTaken = new HashSet<>();
            for (final String loadBalancerName : loadBalancerNames) {
                final Matcher matcher = ApplicationLoadBalancer.ALB_NAME_PATTERN.matcher(loadBalancerName);
                if (matcher.find()) {
                    numbersTaken.add(Integer.parseInt(matcher.group(1)));
                }
            }
            return ApplicationLoadBalancer.DNS_MAPPED_ALB_NAME_PREFIX + IntStream.range(0, ApplicationLoadBalancer.MAX_ALBS_PER_REGION).filter(i->!numbersTaken.contains(i)).min().getAsInt();
        }

        @Override
        public CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            return new CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>(this);
        }

        boolean isForceDNSUpdate() {
            return forceDNSUpdate;
        }
    }
    
    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics, 
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    BuilderT extends Builder<BuilderT, CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>>
    Builder<BuilderT, CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<>();
    }

    private final boolean forceDNSUpdate;

    protected CreateDNSBasedLoadBalancerMapping(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        this.forceDNSUpdate = builder.isForceDNSUpdate();
    }

    @Override
    public void run() throws JSchException, IOException, InterruptedException, SftpException {
        super.run();
        createRoute53Mapping();
    }

    private void createRoute53Mapping() {
        final String hostname = this.getHostName();
        final ApplicationLoadBalancer<ShardingKey> alb = getLoadBalancerUsed();
        getLandscape().setDNSRecordToApplicationLoadBalancer(getLandscape().getDNSHostedZoneId(
                AwsLandscape.getHostedZoneName(hostname)), hostname, alb, forceDNSUpdate);
    }
}
