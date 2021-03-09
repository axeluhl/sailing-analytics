package com.sap.sse.landscape.aws.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.impl.ApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2AsyncClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
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
    private static final Logger logger = Logger.getLogger(AwsApplicationReplicaSetImpl.class.getName());
    private static final String ARCHIVE_SERVER_NAME = "ARCHIVE";
    private static final long serialVersionUID = 6895927683667795173L;
    private final CompletableFuture<AutoScalingGroup> autoScalingGroup;
    private final CompletableFuture<Rule> defaultRedirectRule;
    private final CompletableFuture<String> hostedZoneId;
    private final CompletableFuture<ApplicationLoadBalancer<ShardingKey>> loadBalancer;
    private final CompletableFuture<Iterable<Rule>> loadBalancerRules;
    private final CompletableFuture<TargetGroup<ShardingKey>> masterTargetGroup;
    private final CompletableFuture<TargetGroup<ShardingKey>> publicTargetGroup;
    private final CompletableFuture<ResourceRecordSet> resourceRecordSet;

    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, String hostname, ProcessT master, Optional<Iterable<ProcessT>> replicas,
            CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion,
            CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion) {
        super(replicaSetAndServerName, hostname, master, replicas);
        autoScalingGroup = new CompletableFuture<>();
        defaultRedirectRule = new CompletableFuture<>();
        hostedZoneId = new CompletableFuture<>();
        loadBalancer = new CompletableFuture<>();
        loadBalancerRules = new CompletableFuture<>();
        masterTargetGroup = new CompletableFuture<>();
        publicTargetGroup = new CompletableFuture<>();
        resourceRecordSet = new CompletableFuture<>();
        allLoadBalancersInRegion.thenCompose(loadBalancers->
            allTargetGroupsInRegion.thenCompose(targetGroupsAndTheirTargetHealthDescriptions->
                allLoadBalancerRulesInRegion.handle((listenersAndTheirRules, e)->establishState(loadBalancers, targetGroupsAndTheirTargetHealthDescriptions, listenersAndTheirRules))))
            .handle((v, e)->{
                if (e != null) {
                    logger.log(Level.SEVERE, "Exception while trying to establish state of application replica set "+getName(), e);
                }
                return null;
            });
    }

    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, ProcessT master, Optional<Iterable<ProcessT>> replicas,
            CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion,
            CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion) {
        this(replicaSetAndServerName, /* hostname to be inferred */ null, master, replicas, allLoadBalancersInRegion, allTargetGroupsInRegion, allLoadBalancerRulesInRegion);
    }
    
    /**
     * Tries to complete the various futures of this application replica set, based on the load balancing infrastructure
     * that will be scanned now:
     * 
     * <ul>
     * <li>Find all targets from allTargetGroupsInRegion's TargetHealthDescriptions by comparing the target ID to the
     * master/replica host IDs, and comparing the ProcessT's health check ports to the TargetHealthDescriptions
     * health check ports.</li>
     * 
     * <li>The TargetGroup to which the master's TargetHealthDescription belongs, is remembered as the
     * getMasterTargetGroup(); likewise, those of the replicas are expected to all be the same (at least as long as
     * we don't support sharing here), which is remembered as the getPublicTargetGroup().</li>
     * 
     * <li>Find the Rule(s) in allLoadBalancerRulesInRegion that forward to those target groups; they are all expected
     * to exhibit an equal host-header condition which provides us with the hostname for this replica set. Remember
     * the load balancer(s) (can only be more than one if in the future we support cross-region application replica
     * sets) obtained from the loadBalancerArn that comes with the Listener which keys the Rule lists as the
     * response for getLoadBalancer(). Remember the rules as the result for getLoadBalancerRules().</li>
     * 
     * <li>From the Rule objects determine the one that has a path-pattern of "/" and the host-header condition as the
     * only two conditions and remember that as the result of getDefaultRedirectRule().</li>
     * 
     * <li>Find the archive server(s) based on their SERVER_NAME which can be assumed to be "ARCHIVE" which is
     * expected to not be used by anything else for now. Those should at the same time be the only ProcessT instances
     * not registered in any TargetGroup.</li>
     * 
     * <li>TODO Start to explore the auto scaling infrastructure in order to establish the link from the
     * ApplicationReplicaSet to its AutoScalingGroup(s) (multiple in the future as we may start sharding; then, each
     * shard would have its own AutoScalingGroup and TargetGroup with dedicated routing rules). The AutoScalingGroup
     * can be identified either by name (if we decide for a unique naming pattern) or by enumerating all
     * AutoScalingGroups and filtering for their targetGroupArn.</li>
     * 
     * <li>TODO Discover the Route53 DNS entry pointing to the load balancer with the {@link #hostname} discovered</li>
     * </ul>
     */
    private Void establishState(Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers,
            Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> targetGroupsAndTheirTargetHealthDescriptions,
            Map<Listener, Iterable<Rule>> listenersAndTheirRules) {
        TargetGroup<ShardingKey> myMasterTargetGroup = null;
        for (final Entry<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> e : targetGroupsAndTheirTargetHealthDescriptions.entrySet()) {
            if ((e.getKey().getProtocol() == ProtocolEnum.HTTP || e.getKey().getProtocol() == ProtocolEnum.HTTPS)
            && e.getKey().getLoadBalancerArn() != null && e.getKey().getHealthCheckPort() == getMaster().getPort()) {
                if (!masterTargetGroup.isDone() && !Util.isEmpty(Util.filter(e.getValue(), target->target.target().id().equals(getMaster().getHost().getId())))) {
                    myMasterTargetGroup = e.getKey();
                    masterTargetGroup.complete(myMasterTargetGroup);
                } else if (!publicTargetGroup.isDone() &&
                        !Util.isEmpty(Util.filter(e.getValue(), target->Util.contains(Util.map(getReplicas(), replica->replica.getHost().getId()), target.target().id())))) {
                    publicTargetGroup.complete(e.getKey());
                }
            }
        }
        final TargetGroup<ShardingKey> finalMasterTargetGroup = myMasterTargetGroup;
        // At this point we hope to have found the masterTargetGroup at least, but the publicTargetGroup may not hold the master node, and there may not
        // yet be replicas registered with it; yet, it is possible to discover things from here because from the masterTargetGroup we can infer
        // the hostname header used for routing traffic to the masterTargetGroup, and then we can identify the rule(s) routing to the public target
        // group(s).
        String hostname = null;
        ApplicationLoadBalancer<ShardingKey> myLoadBalancer = null;
        if (finalMasterTargetGroup != null) {
            outer: for (final Entry<Listener, Iterable<Rule>> e : listenersAndTheirRules.entrySet()) {
                for (final Rule rule : e.getValue()) {
                    if (!Util.isEmpty(Util.filter(rule.actions(), action->action.type() == ActionTypeEnum.FORWARD &&
                            !Util.isEmpty(Util.filter(action.forwardConfig().targetGroups(), targetGroup->targetGroup.targetGroupArn().equals(finalMasterTargetGroup.getTargetGroupArn())))))) {
                        // we should be able to extract the hostname from the rule's hostname header condition:
                        hostname = Util.first(Util.filter(rule.conditions(), condition->condition.field().equals("host-header"))).hostHeaderConfig().values().iterator().next();
                        setHostname(hostname);
                        // and we can determine the load balancer via the Listener now:
                        myLoadBalancer = Util.first(Util.filter(loadBalancers, loadBalancer->loadBalancer.getArn().equals(e.getKey().loadBalancerArn())));
                        loadBalancer.complete(myLoadBalancer);
                        break outer;
                    }
                }
            }
        }
        final ApplicationLoadBalancer<ShardingKey> finalLoadBalancer = myLoadBalancer;
        if (hostname != null) {
            final String finalHostname = hostname;
            // now scan the rules in the load balancer identified for the correct hostname and specifically
            // identify the default redirect rule:
            final Set<Rule> rules = new HashSet<>();
            for (final Rule rule : listenersAndTheirRules.entrySet().stream().filter(e->e.getKey().loadBalancerArn().equals(finalLoadBalancer.getArn())).map(e->e.getValue()).findAny().get()) {
                if (rule.conditions().stream().anyMatch(condition->condition.field().equals("host-header") && condition.values().contains(finalHostname))) {
                    if (rule.conditions().stream().anyMatch(condition->condition.field().equals("path-pattern") && condition.values().contains("/"))
                    && rule.actions().stream().anyMatch(action->action.type() == ActionTypeEnum.REDIRECT)) {
                        defaultRedirectRule.complete(rule);
                    }
                    rules.add(rule);
                }
            }
            loadBalancerRules.complete(rules);
            if (!defaultRedirectRule.isDone()) {
                defaultRedirectRule.complete(null); // no default redirect rule found
            }
        } else {
            // we found no target group forwarding to the target, either because the target isn't registered with a target group
            // or no load balancer forwards to it; in any case we can only default to the assumption that the process may be an archive
            // server:
            if (getName().equals(ARCHIVE_SERVER_NAME)) {
                setHostname("www.sapsailing.com");
            } else {
                logger.warning("Found an application replica set " + getName() + " that is not the "
                        + ARCHIVE_SERVER_NAME + " replica set; no hostname can be inferred.");
                setHostname(null);
            }
        }
        // TODO identify autoScalingGroup
        // TODO identify resourceRecordSet
        return null;
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancer() throws InterruptedException, ExecutionException {
        return loadBalancer.get();
    }

    @Override
    public TargetGroup<ShardingKey> getMasterTargetGroup() throws InterruptedException, ExecutionException {
        return masterTargetGroup.get();
    }

    @Override
    public TargetGroup<ShardingKey> getPublicTargetGroup() throws InterruptedException, ExecutionException {
        return publicTargetGroup.get();
    }

    @Override
    public Iterable<Rule> getLoadBalancerRules() throws InterruptedException, ExecutionException {
        return loadBalancerRules.get();
    }

    @Override
    public Rule getDefaultRedirectRule() throws InterruptedException, ExecutionException {
        return defaultRedirectRule.get();
    }

    @Override
    public AwsAutoScalingGroup getAutoScalingGroup() throws InterruptedException, ExecutionException {
        return new AwsAutoScalingGroupImpl(autoScalingGroup.get());
    }

    @Override
    public String getHostedZoneId() throws InterruptedException, ExecutionException {
        return hostedZoneId.get();
    }

    @Override
    public ResourceRecordSet getResourceRecordSet() throws InterruptedException, ExecutionException {
        return resourceRecordSet.get();
    }
}
