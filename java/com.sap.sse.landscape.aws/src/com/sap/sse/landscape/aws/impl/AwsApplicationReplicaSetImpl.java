package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.ServerInfo;
import com.sap.sse.common.Duration;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.impl.ApplicationReplicaSetImpl;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsApplicationProcess;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.ShardName;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2AsyncClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TagDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.model.RRType;
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
public class AwsApplicationReplicaSetImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ApplicationReplicaSetImpl<ShardingKey, MetricsT, ProcessT>
implements AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(AwsApplicationReplicaSetImpl.class.getName());
    private static final String ARCHIVE_SERVER_NAME = "ARCHIVE";
    private static final long serialVersionUID = 6895927683667795173L;
    private final Map<AwsShard<ShardingKey>, Iterable<ShardingKey>> shards;
    private final CompletableFuture<AwsAutoScalingGroup> autoScalingGroup;
    private final CompletableFuture<Rule> defaultRedirectRule;
    private final CompletableFuture<String> hostedZoneId;
    private final CompletableFuture<ApplicationLoadBalancer<ShardingKey>> loadBalancer;
    private final CompletableFuture<Iterable<Rule>> loadBalancerRules;
    private final CompletableFuture<TargetGroup<ShardingKey>> masterTargetGroup;
    private final CompletableFuture<TargetGroup<ShardingKey>> publicTargetGroup;
    private final CompletableFuture<ResourceRecordSet> resourceRecordSet;
    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, String hostname, ProcessT master,
            Optional<Iterable<ProcessT>> replicas,
            CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion,
            CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion,
            CompletableFuture<Iterable<AutoScalingGroup>> allAutoScalingGroups,
            CompletableFuture<Iterable<LaunchConfiguration>> allLaunchConfigurations, DNSCache dnsCache) {
        super(replicaSetAndServerName, hostname, master, replicas);
        autoScalingGroup = new CompletableFuture<>();
        defaultRedirectRule = new CompletableFuture<>();
        hostedZoneId = new CompletableFuture<>();
        loadBalancer = new CompletableFuture<>();
        loadBalancerRules = new CompletableFuture<>();
        masterTargetGroup = new CompletableFuture<>();
        publicTargetGroup = new CompletableFuture<>();
        resourceRecordSet = new CompletableFuture<>();
        shards = new HashMap<>();
        try {
            allLoadBalancersInRegion.thenCompose(loadBalancers -> allTargetGroupsInRegion
                    .thenCompose(targetGroupsAndTheirTargetHealthDescriptions -> allLoadBalancerRulesInRegion
                            .thenCompose(listenersAndTheirRules -> allAutoScalingGroups
                                    .thenCompose(autoScalingGroups -> allLaunchConfigurations
                                            .handle((launchConfigurations, e) -> establishState(loadBalancers,
                                                    targetGroupsAndTheirTargetHealthDescriptions,
                                                    listenersAndTheirRules, autoScalingGroups, launchConfigurations,
                                                    dnsCache))))))
                    .handle((v, e) -> {
                        if (e != null) {
                            logger.log(Level.SEVERE,
                                    "Exception while trying to establish state of application replica set " + getName(),
                                    e);
                        }
                        return null;
                    }).get(Math.round(Landscape.WAIT_FOR_PROCESS_TIMEOUT.get().asMinutes()), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public Map<AwsShard<ShardingKey>, Iterable<ShardingKey>> getShards() throws Exception{
        return shards;
    }

    public AwsApplicationReplicaSetImpl(String replicaSetAndServerName, ProcessT master,
            Optional<Iterable<ProcessT>> replicas,
            CompletableFuture<Iterable<ApplicationLoadBalancer<ShardingKey>>> allLoadBalancersInRegion,
            CompletableFuture<Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>>> allTargetGroupsInRegion,
            CompletableFuture<Map<Listener, Iterable<Rule>>> allLoadBalancerRulesInRegion,
            AwsLandscape<ShardingKey> landscape, CompletableFuture<Iterable<AutoScalingGroup>> allAutoScalingGroups,
            CompletableFuture<Iterable<LaunchConfiguration>> allLaunchConfigurations, DNSCache dnsCache) {
        this(replicaSetAndServerName, /* hostname to be inferred */ null, master, replicas, allLoadBalancersInRegion,
                allTargetGroupsInRegion, allLoadBalancerRulesInRegion, allAutoScalingGroups, allLaunchConfigurations,
                dnsCache);
    }
    
    /**
     * Tries to complete the various futures of this application replica set, based on the load balancing infrastructure
     * that will be scanned now:
     * 
     * <ul>
     * <li>Find all targets from allTargetGroupsInRegion's TargetHealthDescriptions by comparing the target ID to the
     * master/replica host IDs, and comparing the ProcessT's health check ports to the TargetHealthDescriptions health
     * check ports.</li>
     * 
     * <li>The TargetGroup to which the master's TargetHealthDescription belongs, is remembered as the
     * getMasterTargetGroup(); likewise, those of the replicas are expected to all be the same (at least as long as we
     * don't support sharing here), which is remembered as the getPublicTargetGroup().</li>
     * 
     * <li>Find the Rule(s) in allLoadBalancerRulesInRegion that forward to those target groups; they are all expected
     * to exhibit an equal host-header condition which provides us with the hostname for this replica set. Remember the
     * load balancer(s) (can only be more than one if in the future we support cross-region application replica sets)
     * obtained from the loadBalancerArn that comes with the Listener which keys the Rule lists as the response for
     * getLoadBalancer(). Remember the rules as the result for getLoadBalancerRules().</li>
     * 
     * <li>From the Rule objects determine the one that has a path-pattern of "/" and the host-header condition as the
     * only two conditions and remember that as the result of getDefaultRedirectRule().</li>
     * 
     * <li>Find the archive server(s) based on their SERVER_NAME which can be assumed to be "ARCHIVE" which is expected
     * to not be used by anything else for now. Those should at the same time be the only ProcessT instances not
     * registered in any TargetGroup.</li>
     * 
     * <li>Discover the Route53 DNS entry pointing to the load balancer with the {@link #hostname} discovered</li>
     * 
     * <li>Explore the auto scaling infrastructure in order to establish the link from this
     * {@link ApplicationReplicaSet} to its {@link AutoScalingGroup}(s) (multiple in the future as we may start
     * sharding; then, each shard would have its own {@link AutoScalingGroup} and {@link TargetGroup} with dedicated
     * routing rules). The {@link AutoScalingGroup} can be identified by enumerating all {@link AutoScalingGroup}s and
     * filtering for their {@link TargetGroup#getTargetGroupArn() targetGroupArn}.</li>
     * </ul>
     */
    private Void establishState(Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers,
            Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> targetGroupsAndTheirTargetHealthDescriptions,
            Map<Listener, Iterable<Rule>> listenersAndTheirRules, Iterable<AutoScalingGroup> autoScalingGroups,
            Iterable<LaunchConfiguration> launchConfigurations, DNSCache dnsCache) {
        TargetGroup<ShardingKey> myMasterTargetGroup = null;
        TargetGroup<ShardingKey> singleTargetGroupCandidate = null;
        for (final Entry<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> e : targetGroupsAndTheirTargetHealthDescriptions.entrySet()) {
            if ((e.getKey().getProtocol() == ProtocolEnum.HTTP || e.getKey().getProtocol() == ProtocolEnum.HTTPS)
            && e.getKey().getLoadBalancerArn() != null && e.getKey().getHealthCheckPort() == getMaster().getPort()) {
                // an HTTP(S) target group that is currently active in a load balancer and forwards to this replica set master's port
                if (!masterTargetGroup.isDone() && !Util.isEmpty(Util.filter(e.getValue(), target->target.target().id().equals(getMaster().getHost().getId())))) {
                    if (hasMasterRuleForward(listenersAndTheirRules, e.getKey())) {
                        // this replica set's master is registered with the target group, and there is a rule that forwards
                        // requests with explicit master-markup to this target group:
                        myMasterTargetGroup = e.getKey();
                        masterTargetGroup.complete(myMasterTargetGroup);
                    } else {
                        // no explicit master rule forward found, but the target group still forwards to our master;
                        // keep in mind as a candidate if this is a set-up with only a single target group:
                        singleTargetGroupCandidate = e.getKey();
                    }
                }
                if (!publicTargetGroup.isDone()
                        && (!Util.isEmpty(Util.filter(e.getValue(),
                                target -> Util.contains(Util.map(getReplicas(), replica -> replica.getHost().getId()),
                                        target.target().id())))
                                || !Util.isEmpty(Util.filter(e.getValue(),
                                        target -> target.target().id().equals(getMaster().getHost().getId()))))
                        && hasPublicRuleForward(listenersAndTheirRules, e.getKey())
                        && !hasPathCondition(listenersAndTheirRules, e.getKey())) {
                    // this replica set's master or at least one of the replicas of this replica set is registered with
                    // the target group, and there is a rule that forwards
                    // requests with explicit replica-markup to this target group:
                    publicTargetGroup.complete(e.getKey());
                    tryToFindAutoScalingGroup(e.getKey(), autoScalingGroups, launchConfigurations);
                }
            }   
        }
        shards.putAll(establishShards(targetGroupsAndTheirTargetHealthDescriptions, listenersAndTheirRules,
                autoScalingGroups, launchConfigurations));
        if (!autoScalingGroup.isDone()) { // no auto-scaling group was found after having looked at all target groups
            autoScalingGroup.complete(null);
        }
        // "legacy" case where a single target group handles all requests
        if (!masterTargetGroup.isDone() && !publicTargetGroup.isDone() && singleTargetGroupCandidate != null) {
            myMasterTargetGroup = singleTargetGroupCandidate;
            masterTargetGroup.complete(singleTargetGroupCandidate);
            publicTargetGroup.complete(singleTargetGroupCandidate);
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
                        dnsCache.getHostedZoneId(AwsLandscape.getHostedZoneName(hostname)).handle((hzid, ex)->hostedZoneId.complete(hzid));
                        dnsCache.getResourceRecordSetsAsync(hostname).thenAccept(resourceRecordSets->{
                            final Optional<ResourceRecordSet> firstResourceRecordSet = Util.stream(resourceRecordSets).findFirst();
                            if (!firstResourceRecordSet.isPresent()) {
                                resourceRecordSet.complete(null);
                            } else {
                                firstResourceRecordSet.ifPresent(rrs->{
                                    resourceRecordSet.complete(rrs);
                                    try {
                                        if (rrs.type() == RRType.CNAME && !Util.isEmpty(Util.filter(rrs.resourceRecords(), rr->{
                                            try {
                                                return rr.value().equals(getLoadBalancer().getDNSName());
                                            } catch (InterruptedException | ExecutionException e1) {
                                                logger.log(Level.WARNING, "This shouldn't have happened", e1);
                                                throw new RuntimeException(e1);
                                            }
                                        }))) {
                                            logger.fine("Found DNS resource record "+getHostname()+" pointing to application replica set's load balancer "+getLoadBalancer().getArn());
                                        }
                                    } catch (InterruptedException | ExecutionException e1) {
                                        logger.log(Level.WARNING, "This shouldn't have happened", e1);
                                    }
                                });
                            }
                        });
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
            logger.fine("Couldn't find hostname, target group(s) and rules for "+getName());
            loadBalancerRules.complete(Collections.emptySet());
            defaultRedirectRule.complete(null); // no default redirect rule found
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
        return null;
    }

    AwsAutoScalingGroup getShardAutoscalinggroup(TargetGroup<ShardingKey> targetGroup,
            Iterable<AutoScalingGroup> autoScalingGroups, Iterable<LaunchConfiguration> launchConfigurations) {
        AwsAutoScalingGroup autoscalinggroup = Util
                .stream(autoScalingGroups).filter(
                        autoScalingGroup -> autoScalingGroup.targetGroupARNs()
                                .contains(targetGroup.getTargetGroupArn()))
                .findFirst()
                .map(asg -> new AwsAutoScalingGroupImpl(asg,
                        Util.filter(launchConfigurations,
                                lc -> Util.equalsWithNull(lc.launchConfigurationName(), asg.launchConfigurationName()))
                                .iterator().next(),
                        targetGroup.getRegion()))
                .orElse(null);
        return autoscalinggroup;
    }

    @SuppressWarnings("unchecked")
    private HashMap<AwsShard<ShardingKey>, Iterable<ShardingKey>> establishShards(
            Map<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> targetGroupsAndTheirTargetHealthDescriptions,
            Map<Listener, Iterable<Rule>> listenersAndTheirRules, Iterable<AutoScalingGroup> autoScalingGroups,
            Iterable<LaunchConfiguration> launchConfigurations) {
        HashMap<AwsShard<ShardingKey>, Iterable<ShardingKey>> shardMap = new HashMap<>();
        for (final Entry<TargetGroup<ShardingKey>, Iterable<TargetHealthDescription>> e : targetGroupsAndTheirTargetHealthDescriptions
                .entrySet()) {
            if ((e.getKey().getProtocol() == ProtocolEnum.HTTP || e.getKey().getProtocol() == ProtocolEnum.HTTPS)
                    && e.getKey().getLoadBalancerArn() != null
                    && e.getKey().getHealthCheckPort() == getMaster().getPort()) {
                // an HTTP(S) target group that is currently active in a load balancer and forwards to this replica
                // set master's port
                final Set<Rule> pathRules = getListenerRulesWithPathToReplica(listenersAndTheirRules, e.getKey());
                if (!pathRules.isEmpty() && isShardName(e.getKey().getName())) {
                    // Is shard
                    Set<String> keys = getShardingKeys(listenersAndTheirRules, e.getKey());
                    String tagName = null;
                    Iterable<TagDescription> tagsDesc = e.getKey().getTagDescriptions();
                    for (TagDescription des : tagsDesc) {
                        Iterable<Tag> tag = Util.filter(des.tags(), t -> t.key().equals(ShardName.TAG_KEY));
                        if (!Util.isEmpty(tag)) {
                            tagName = tag.iterator().next().value();
                            break;
                        }
                    }
                    ShardName shardName;
                    try {
                        shardName = ShardName.parse(e.getKey().getName(), tagName);
                    } catch (Exception e1) {
                        logger.info(e1.getMessage());
                        // This entry is no valid shard
                        continue;
                    }
                    AwsShardImpl<ShardingKey> shard = new AwsShardImpl<ShardingKey>(getName(),
                            Util.asList(Util.map(keys, s -> (ShardingKey) s)), e.getKey(), shardName,
                            e.getKey().getLoadBalancer(), pathRules);
                    shardMap.put(shard, shard.getKeys());
                    shard.setAutoscalingGroup(
                            getShardAutoscalinggroup(e.getKey(), autoScalingGroups, launchConfigurations));
                }
            }
        }
        return shardMap;
    };
    
    private boolean isShardName(String requestedName) {
            return ShardName.isValidTargetGroupName(requestedName);
    }
    
    @Override
    public ShardName getNewShardName(String shardName) throws Exception{
        return ShardName.create(getName(), shardName);
    }
    
    @Override
    public boolean isEligibleForDeployment(ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> host,
            Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        boolean result;
        if (host.isManagedByAutoScalingGroup()) {
            result = false;
        } else {
            result = true;
            final Iterable<ProcessT> applicationProcesses = host.getApplicationProcesses(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
            for (final ProcessT applicationProcess : applicationProcesses) {
                if (applicationProcess.getPort() == getPort() || applicationProcess.getServerName(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase).equals(getServerName())) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void stopAllUnmanagedReplicas(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        logger.info("Stopping all unmanaged replicas of replica set "+this);
        for (final ProcessT replica : getReplicas()) {
            if (getAutoScalingGroup() == null || !replica.getHost().isManagedByAutoScalingGroup(Collections.singleton(getAutoScalingGroup()))) {
                logger.info("Found unmanaged replica "+replica+". Removing from public target group and stopping...");
                getPublicTargetGroup().removeTarget(replica.getHost());
                replica.stopAndTerminateIfLast(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
            }
        }
    }

    private boolean hasPublicRuleForward(Map<Listener, Iterable<Rule>> listenersAndTheirRules, TargetGroup<ShardingKey> publicTargetGroupCandidate) {
        return hasListenerRuleWithHostHeaderForward(listenersAndTheirRules, publicTargetGroupCandidate, HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB());
    }

    private boolean hasPathCondition(Map<Listener, Iterable<Rule>> listenersAndTheirRules,
            TargetGroup<ShardingKey> shardingTargetGroupCandidate) {
        final String shardTargetGroupCandidateArn = shardingTargetGroupCandidate.getTargetGroupArn();
        for (final Entry<Listener, Iterable<Rule>> e : listenersAndTheirRules.entrySet()) {
            if (Util.equalsWithNull(e.getKey().loadBalancerArn(), shardingTargetGroupCandidate.getLoadBalancerArn())) {
                for (final Rule rule : e.getValue()) {
                    for (final Action action : rule.actions()) {
                        if (action.type() == ActionTypeEnum.FORWARD) {
                            for (final TargetGroupTuple targetGroupTuple : action.forwardConfig().targetGroups()) {
                                if (Util.equalsWithNull(targetGroupTuple.targetGroupArn(),
                                        shardTargetGroupCandidateArn)) {
                                    for (final RuleCondition condition : rule.conditions()) {
                                        if (Util.equalsWithNull(condition.field(), "path-pattern")) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasListenerRuleWithHostHeaderForward(Map<Listener, Iterable<Rule>> listenersAndTheirRules, TargetGroup<ShardingKey> publicTargetGroupCandidate, String hostHeaderForwardTo) {
        final String publicTargetGroupCandidateArn = publicTargetGroupCandidate.getTargetGroupArn();
        for (final Entry<Listener, Iterable<Rule>> e : listenersAndTheirRules.entrySet()) {
            if (Util.equalsWithNull(e.getKey().loadBalancerArn(), publicTargetGroupCandidate.getLoadBalancerArn())) {
                for (final Rule rule : e.getValue()) {
                    for (final Action action : rule.actions()) {
                        if (action.type() == ActionTypeEnum.FORWARD) {
                            for (final TargetGroupTuple targetGroupTuple : action.forwardConfig().targetGroups()) {
                                if (Util.equalsWithNull(targetGroupTuple.targetGroupArn(), publicTargetGroupCandidateArn)) {
                                    for (final RuleCondition condition : rule.conditions()) {
                                        if (Util.equalsWithNull(condition.field(), "http-header")
                                         && Util.equalsWithNull(condition.httpHeaderConfig().httpHeaderName(), HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                         && condition.httpHeaderConfig().values().contains(hostHeaderForwardTo)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private Set<Rule> getListenerRulesWithPathToReplica(Map<Listener, Iterable<Rule>> listenersAndTheirRules,
            TargetGroup<ShardingKey> shardTargetGroupCandidate) {
        final String publicTargetGroupCandidateArn = shardTargetGroupCandidate.getTargetGroupArn();
        Set<Rule> res = new HashSet<Rule>();
        for (final Entry<Listener, Iterable<Rule>> e : listenersAndTheirRules.entrySet()) {
            if (Util.equalsWithNull(e.getKey().loadBalancerArn(), shardTargetGroupCandidate.getLoadBalancerArn())) {
                for (final Rule rule : e.getValue()) {
                    for (final Action action : rule.actions()) {
                        if (action.type() == ActionTypeEnum.FORWARD) {
                            for (final TargetGroupTuple targetGroupTuple : action.forwardConfig().targetGroups()) {
                                if (Util.equalsWithNull(targetGroupTuple.targetGroupArn(),
                                        publicTargetGroupCandidateArn)) {
                                    for (final RuleCondition condition : rule.conditions()) {
                                        if (Util.equalsWithNull(condition.field(), "path-pattern")) {
                                            for (final RuleCondition ruleCondition : rule.conditions()) {
                                                if (Util.equalsWithNull(ruleCondition.field(), "http-header")
                                                        && Util.equalsWithNull(
                                                                ruleCondition.httpHeaderConfig().httpHeaderName(),
                                                                HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA
                                                                        .getA())
                                                        && ruleCondition.httpHeaderConfig().values().contains(
                                                                HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA
                                                                        .getB())) {
                                                    res.add(rule);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    private Set<String> getShardingKeys(Map<Listener, Iterable<Rule>> listenersAndTheirRules,
            TargetGroup<ShardingKey> shardTargetGroupCandidate) {
        final String publicTargetGroupCandidateArn = shardTargetGroupCandidate.getTargetGroupArn();
        Set<String> res = new HashSet<>();
        for (final Entry<Listener, Iterable<Rule>> e : listenersAndTheirRules.entrySet()) {
            if (Util.equalsWithNull(e.getKey().loadBalancerArn(), shardTargetGroupCandidate.getLoadBalancerArn())) {
                for (final Rule rule : e.getValue()) {
                    for (final Action action : rule.actions()) {
                        if (action.type() == ActionTypeEnum.FORWARD) {
                            for (final TargetGroupTuple targetGroupTuple : action.forwardConfig().targetGroups()) {
                                if (Util.equalsWithNull(targetGroupTuple.targetGroupArn(),
                                        publicTargetGroupCandidateArn)) {
                                    for (final RuleCondition condition : rule.conditions()) {
                                        if (Util.equalsWithNull(condition.field(), "http-header")
                                                && Util.equalsWithNull(condition.httpHeaderConfig().httpHeaderName(),
                                                        HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getA())
                                                && condition.httpHeaderConfig().values().contains(
                                                        HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB())) {
                                            for (final RuleCondition ruleCondition : rule.conditions()) {
                                                if (Util.equalsWithNull(ruleCondition.field(), "path-pattern")) {
                                                    res.addAll(ruleCondition.values());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    private boolean hasMasterRuleForward(Map<Listener, Iterable<Rule>> listenersAndTheirRules, TargetGroup<ShardingKey> masterTargetGroupCandidate) {
        return hasListenerRuleWithHostHeaderForward(listenersAndTheirRules, masterTargetGroupCandidate, HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER.getB());
    }

    /**
     * Completes the {@link #autoScalingGroup} with {@code null} if not found
     */
    private void tryToFindAutoScalingGroup(TargetGroup<ShardingKey> targetGroup, Iterable<AutoScalingGroup> autoScalingGroups, Iterable<LaunchConfiguration> launchConfigurations) {
        autoScalingGroup.complete(
            Util.stream(autoScalingGroups).filter(autoScalingGroup->autoScalingGroup.targetGroupARNs().contains(targetGroup.getTargetGroupArn())).
                findFirst().map(asg->
                    new AwsAutoScalingGroupImpl(asg,
                            Util.filter(launchConfigurations, lc->Util.equalsWithNull(lc.launchConfigurationName(), asg.launchConfigurationName())).iterator().next(),
                            targetGroup.getRegion())).orElse(null));
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
        return autoScalingGroup.get();
    }

    @Override
    public String getHostedZoneId() throws InterruptedException, ExecutionException {
        return hostedZoneId.get();
    }

    @Override
    public ResourceRecordSet getResourceRecordSet() throws InterruptedException, ExecutionException {
        return resourceRecordSet.get();
    }

    @Override
    public void restartAllReplicas(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ProcessT replica : getReplicas()) {
            logger.info("Restarting replica "+replica+" in replica set "+getName());
            try {
                replica.restart(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
                logger.info("Wating until restarted replica "+replica+" in replica set "+getName()+" has become ready:");
                replica.waitUntilReady(optionalTimeout);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Problem restarting replica "+replica+". Continuing by restarting the next replica if there are more.", e);
            }
        }
    }

    @Override
    public boolean isLocalReplicaSet() {
        return getName().equals(ServerInfo.getName());
    }
    
    @Override
    public void removeShard(AwsShard<ShardingKey> shard, AwsLandscape<ShardingKey> landscape) throws Exception {
        Collection<TargetGroup<ShardingKey>> targetGroups = new ArrayList<>();
        targetGroups.add(shard.getTargetGroup());
        // remove rules for targetgrouo
        landscape.deleteLoadBalancerListenerRules(shard.getTargetGroup().getRegion(),
                Util.toArray(getLoadBalancer().getRulesForTargetGroups(targetGroups), new Rule[0]));
        // remove autoscaling group
        landscape.removeAutoScalingGroup(shard.getAutoScalingGroup());
        // remove targetgroup
        landscape.deleteTargetGroup(shard.getTargetGroup());
    }
}