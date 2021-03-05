package com.sap.sse.landscape.aws.impl;

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
            Optional<Iterable<ProcessT>> replicas) {
        super(replicaSetAndServerName, master, replicas);
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
