package com.sap.sse.landscape.aws.orchestration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.orchestration.Procedure;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;

/**
 * For an {@link ApplicationProcess} creates a DNS-based load balancer set-up. This entails setting up a Route53 DNS entry
 * for the hostname, pointing to the load balancer's {@link ApplicationLoadBalancer#getDNSName() DNS name} as a CNAME record.
 * Furthermore, this procedure has logic to provision a new ALB in case no other DNS-mapped ALB has sufficient rule capacity
 * (see {@link #MAX_RULES_PER_ALB}) to accommodate the {@link #NUMBER_OF_RULES_PER_REPLICA_SET} additional rules required.
 * The naming scheme for those DNS-mapped ALBs is expected to follow the pattern {@link #DNS_MAPPED_ALB_NAME_PREFIX}{@code [0-9]*} which
 * means that names such as "DNSMapped-4" and "DNSMapped-7" are valid names.
 * 
 * @author Axel Uhl (D043530)
 */
public class CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>>
extends CreateLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private static final String DNS_MAPPED_ALB_NAME_PREFIX = "DNSMapped-";
    private static final Pattern ALB_NAME_PATTERN = Pattern.compile(DNS_MAPPED_ALB_NAME_PREFIX+"(.*)$");
    
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> build() throws JSchException, IOException, InterruptedException, SftpException;
    }
    
    protected static class BuilderImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends CreateLoadBalancerMapping.BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
    implements Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        @Override
        public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancerUsed() throws InterruptedException {
            final ApplicationLoadBalancer<ShardingKey, MetricsT> result;
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
         * @throws InterruptedException 
         */
        private ApplicationLoadBalancer<ShardingKey, MetricsT> getOrCreateDNSMappedLoadBalancer(
                AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, Region region) throws InterruptedException {
            ApplicationLoadBalancer<ShardingKey, MetricsT> result = null;
            final Set<String> loadBalancerNames = new HashSet<>();
            for (final ApplicationLoadBalancer<ShardingKey, MetricsT> loadBalancer : landscape.getLoadBalancers(region)) {
                if (ALB_NAME_PATTERN.matcher(loadBalancer.getName()).matches()) {
                    loadBalancerNames.add(loadBalancer.getName());
                    if (Util.size(loadBalancer.getRules()) <= MAX_RULES_PER_ALB - NUMBER_OF_RULES_PER_REPLICA_SET) {
                        result = loadBalancer;
                        break;
                    }
                }
            }
            if (result == null) {
                result = landscape.createLoadBalancer(getAvailableDNSMappedAlbName(loadBalancerNames), region);
                final TimePoint startingToPollForReady = TimePoint.now();
                while (landscape.getApplicationLoadBalancerStatus(result).code() == LoadBalancerStateEnum.PROVISIONING
                        && (!getOptionalTimeout().isPresent() || startingToPollForReady.until(TimePoint.now()).compareTo(getOptionalTimeout().get()) <= 0)) {
                    Thread.sleep(1000); // wait until the ALB has been provisioned or failed
                }
            }
            return result;
        }

        /**
         * Picks a new load balancer name following the pattern {@link #DNS_MAPPED_ALB_NAME_PREFIX}{@code [0-9]+} that is not
         * part of {@code loadBalancerNames} and has the least number.
         */
        private String getAvailableDNSMappedAlbName(Set<String> loadBalancerNames) {
            final Set<Integer> numbersTaken = new HashSet<>();
            for (final String loadBalancerName : loadBalancerNames) {
                final Matcher matcher = ALB_NAME_PATTERN.matcher(loadBalancerName);
                if (matcher.find()) {
                    numbersTaken.add(Integer.parseInt(matcher.group(1)));
                }
            }
            return DNS_MAPPED_ALB_NAME_PREFIX + IntStream.range(0, MAX_ALBS_PER_REGION).filter(i->!numbersTaken.contains(i)).min().getAsInt();
        }

        @Override
        public CreateDNSBasedLoadBalancerMapping<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> build() throws JSchException, IOException, InterruptedException, SftpException {
            return new CreateDNSBasedLoadBalancerMapping<>(this);
        }
    }
    
    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, HostT extends AwsInstance<ShardingKey, MetricsT>>
    Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder() {
        return new BuilderImpl<>();
    }

    protected CreateDNSBasedLoadBalancerMapping(BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) throws JSchException, IOException, InterruptedException, SftpException {
        super(builder);
    }

    @Override
    public void run() throws JSchException, IOException, InterruptedException, SftpException {
        super.run();
        createRoute53Mapping();
    }

    private void createRoute53Mapping() {
        final String hostname = this.getHostName();
        final ApplicationLoadBalancer<ShardingKey, MetricsT> alb = getLoadBalancerUsed();
        getLandscape().setDNSRecordToApplicationLoadBalancer(getLandscape().getDNSHostedZoneId(
                getHostedZoneName(hostname)), hostname, alb);
    }
}
