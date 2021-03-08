package com.sap.sse.landscape.aws.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
            Optional<Iterable<ProcessT>> replicas, CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Iterable<TargetGroup<ShardingKey>>> allTargetGroupsInRegion, CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion) {
        super(replicaSetAndServerName, master, replicas);
        /*
         * TODO: to make things more efficient we should acquire all ApplicationLoadBalancer objects in the region in
         * one round trip, then fetch all TargetGroup objects in a second round trip, and all TargetHealthDescriptions
         * in a third round-trip; all can run asynchronously, see ElasticLoadBalancingV2AsyncClient. In a fourth round
         * trip, we should fetch all Rule objects for all HTTPS load balancer Listener objects. With this, we then have
         * all information about how requests are routed. Additionally, we may explore the auto scaling infrastructure
         * in order to establish the link from the ApplicationReplicaSet to their AutoScalingGroup(s) (multiple in the
         * future as we may start sharding; then, each shard would have its own AutoScalingGroup and TargetGroup with
         * dedicated routing rules).
         * 
         * The ApplicationReplicaSet could then know its Rule objects, the responsible ApplicationLoadBalancer and the
         * master TargetGroup plus one (or in the future more, see above) public target groups with the registered targets.
         * We could in principle even discover the ApplicationReplicaSet objects starting from the load balancers, only that
         * then we wouldn't find the archive server(s) as they are currently not modeled with a dedicated load balancer Rule
         * but instead are reached through the default Rule that forwards all *.sapsailing.com traffic not otherwise routed
         * into the central reverse proxy from where it gets forwarded to the Archive server.
         * 
         * Otherwise, the exploration of the ApplicationProcess instances is a bit time consuming, and the most we get out
         * of it currently is the serverDirectory property which would soon be ApplicationProcessHost.DEFAULT_SERVERS_PATH/${SERVER_NAME}
         * for all instances (after their next migration) anyhow. Currently, we're exploring ApplicationProcessHost.DEFAULT_SERVERS_PATH
         * for subdirectories for which we then create the ApplicationProcess instances.
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
