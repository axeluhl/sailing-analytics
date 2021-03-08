package com.sap.sse.landscape.aws.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.impl.ApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2AsyncClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

/**
 * The implementation of those methods requiring landscape "introspection" works asynchronously, triggered at construction time, using
 * mostly {@link ElasticLoadBalancingV2AsyncClient} and {@link Route53AsyncClient} functionality. The {@link CompletableFuture} objects
 * returned by those APIs will be {@link CompletableFuture#handleAsync(java.util.function.BiFunction) chained} to finally deliver a
 * {@link CompletableFuture} for each of the things the getters on this class expose. When these are called, it therefore may lead to
 * some blocking / waiting time in case the {@link CompletableFuture} that delivers that value hasn't completed yet.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public class AwsApplicationReplicaSetImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ApplicationReplicaSetImpl<ShardingKey, MetricsT, ProcessT>
implements AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> {
    private static final long serialVersionUID = 6895927683667795173L;

    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, String hostname, ProcessT master, Optional<Iterable<ProcessT>> replicas) {
        super(replicaSetAndServerName, hostname, master, replicas);
        // TODO Auto-generated constructor stub
    }
    
    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, ProcessT master,
            Optional<Iterable<ProcessT>> replicas,
            CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Iterable<Pair<TargetGroup<ShardingKey>, CompletableFuture<Iterable<TargetHealthDescription>>>>> allTargetGroupsInRegion,
            CompletableFuture<Iterable<Pair<Listener, CompletableFuture<List<Rule>>>>> allLoadBalancerRulesInRegion) {
        super(replicaSetAndServerName, master, replicas);
        /*
         * TODO: With this, we have all information about how requests are routed:
         * 
         * - Find all targets from allTargetGroupsInRegion's TargetHealthDescriptions by comparing the target ID to the
         * master/replica host IDs, and comparing the ProcessT's health check ports to the TargetHealthDescriptions
         * health check ports.
         * 
         * - The TargetGroup to which the master's TargetHealthDescription belongs, is remembered as the
         * getMasterTargetGroup(); likewise, those of the replicas are expected to all be the same (at least as long as
         * we don't support sharing here), which is remembered as the getPublicTargetGroup().
         * 
         * - Find the Rule(s) in allLoadBalancerRulesInRegion that forward to those target groups; they are all expected
         * to exhibit an equal host-header condition which provides us with the hostname for this replica set. Remember
         * the load balancer(s) (can only be more than one if in the future we support cross-region application replica
         * sets) obtained from the loadBalancerArn that comes with the Listener which keys the Rule lists as the
         * response for getLoadBalancer(). Remember the rules as the result for getLoadBalancerRules().
         * 
         * - From the Rule objects determine the one that has a path-pattern of "/" and the host-header condition as the
         * only two conditions and remember that as the result of getDefaultRedirectRule().
         * 
         * - Start to explore the auto scaling infrastructure in order to establish the link from the
         * ApplicationReplicaSet to its AutoScalingGroup(s) (multiple in the future as we may start sharding; then, each
         * shard would have its own AutoScalingGroup and TargetGroup with dedicated routing rules). The AutoScalingGroup
         * can be identified either by name (if we decide for a unique naming pattern) or by enumerating all
         * AutoScalingGroups and filtering for their targetGroupArn.
         * 
         * - Find the archive server(s) based on their SERVER_NAME which can be assumed to be "ARCHIVE" which is
         * expected to not be used by anything else for now. Those should at the same time be the only ProcessT instances
         * not registered in any TargetGroup.
         * 
         */
        // TODO Auto-generated constructor stub
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancer() {
        // TODO Implement AwsApplicationReplicaSetImpl.getLoadBalancer(...)
        return null;
    }

    @Override
    public TargetGroup<ShardingKey> getMasterTargetGroup() {
        // TODO Implement AwsApplicationReplicaSetImpl.getMasterTargetGroup(...)
        return null;
    }

    @Override
    public TargetGroup<ShardingKey> getPublicTargetGroup() {
        // TODO Implement AwsApplicationReplicaSetImpl.getPublicTargetGroup(...)
        return null;
    }

    @Override
    public Iterable<Rule> getLoadBalancerRules() {
        // TODO Implement AwsApplicationReplicaSetImpl.getLoadBalancerRules(...)
        return null;
    }

    @Override
    public Rule getDefaultRedirectRule() {
        // TODO Implement AwsApplicationReplicaSetImpl.getDefaultRedirectRule(...)
        return null;
    }

    @Override
    public AwsAutoScalingGroup getAutoScalingGroup() {
        // TODO Implement AwsApplicationReplicaSetImpl.getAutoScalingGroup(...)
        return null;
    }

    @Override
    public String getHostedZoneId() {
        // TODO Implement AwsApplicationReplicaSetImpl.getHostedZoneId(...)
        return null;
    }

    @Override
    public ResourceRecordSet getResourceRecordSet() {
        // TODO Implement AwsApplicationReplicaSetImpl.getResourceRecordSet(...)
        return null;
    }
}
