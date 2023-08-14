package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter;
import com.sap.sse.landscape.aws.impl.ShardingRuleManagementHelper;
import com.sap.sse.landscape.aws.impl.ShardingRulePathConditionBuilder;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ForwardActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;
/**
 * This class is the base for all procedures that deal with shards. In the subclasses, all procedures are described.
 * The parameter <ShardingKey> stands for a type that represents a sharding key. But those keys are normally strings because AWS deals with them as strings in
 * their Rules. The function Shard.getKeys returns a list of the keys contained by the shard. Those keys are found in the shard's rules as a path-condition.
 * This class implements most of the required functionality when dealing with shards like inserting rules or switching a replica set's load balancer. 
 * @author I569653
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public abstract class ShardProcedure<ShardingKey,
    MetricsT extends ApplicationProcessMetrics, 
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractAwsProcedureImpl<ShardingKey>
implements ProcedureCreatingLoadBalancerMapping<ShardingKey> {
    private static final Logger logger = Logger.getLogger(ShardProcedure.class.getName());
    public final static int NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE = 2;
    public static final int DEFAULT_MINIMUM_AUTO_SCALING_GROUP_SIZE = 2;
    @SuppressWarnings("unchecked") // this silently assumes that a String can be cast to a ShardingKey without problems
    protected final ShardingKey SHARDING_KEY_UNUSED_BY_ANY_APPLICATION = (ShardingKey) "lauycaluy3cla3yrclaurlIYQL8";
    protected final String shardName;
    final protected Set<ShardingKey> shardingKeys;
    final protected AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet;
    final protected Region region;

    protected ShardProcedure(BuilderImpl<?,?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        this.shardName = builder.getShardName();
        this.replicaSet = builder.getReplicaSet();
        this.shardingKeys = builder.getShardingKeys();
        this.region = builder.getRegion();
    }

    public static interface Builder<
        BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
        T extends ShardProcedure<ShardingKey, MetricsT, ProcessT>, 
        ShardingKey, 
        MetricsT extends ApplicationProcessMetrics, 
        ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends AbstractAwsProcedureImpl.Builder<BuilderT, T, ShardingKey> {
        BuilderT setPathPrefixForShardingKey(String pathPrefixForShardingKey);
        
        BuilderT setShardName(String name);

        BuilderT setLandscape(AwsLandscape<String> landscape);

        BuilderT setShardingKeys(Set<ShardingKey> shardingkeys);

        BuilderT setReplicaSet(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset);

        BuilderT setRegion(Region region);
    }

    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>, 
        T extends ShardProcedure<ShardingKey,MetricsT,ProcessT>,
        ShardingKey, 
        MetricsT extends ApplicationProcessMetrics, 
        ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends
            AbstractAwsProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey>
            implements
            Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        protected String shardName;
        protected Set<ShardingKey> shardingKeys;
        protected AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet;
        protected Region region;
        private String pathPrefixForShardingKey;
        
        @Override
        public BuilderT setPathPrefixForShardingKey(String pathPrefixForShardingKey) {
            this.pathPrefixForShardingKey = pathPrefixForShardingKey;
            return self();
        }

        @Override
        public BuilderT setShardName(String name) {
            this.shardName = name;
            return self();
        }

        @Override
        public BuilderT setShardingKeys(Set<ShardingKey> shardingkeys) {
            this.shardingKeys = shardingkeys;
            return self();
        }

        @Override
        public BuilderT setReplicaSet(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset) {
            this.replicaSet = replicaset;
            return self();
        }

        @Override
        public BuilderT setRegion(Region region) {
            this.region = region;
            return self();
        }

        @SuppressWarnings("unchecked")
        @Override
        public BuilderT setLandscape(AwsLandscape<String> landscape) {
            super.setLandscape((AwsLandscape<ShardingKey>) landscape);
            return self();
        }

        protected AwsLandscape<ShardingKey> getLandscape() {
            return (AwsLandscape<ShardingKey>) super.getLandscape();
        }

        Region region() {
            return region;
        }

        AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> getReplicaSet() {
            return replicaSet;
        }

        Set<ShardingKey> getShardingKeys() {
            return shardingKeys;
        }

        String getShardName() {
            return shardName;
        }

        Region getRegion() {
            return region;
        }
        
        String getPathPrefixForShardingKey() {
            return pathPrefixForShardingKey;
        }
    }
    
    protected boolean isTargetGroupNameUnique(String name) {
        final Iterable<TargetGroup<ShardingKey>> targetGroups = getLandscape().getTargetGroups(region);
        return Util.isEmpty(Util.filter(targetGroups, t -> t.getName().equals(name)));   
    }

    protected Collection<RuleCondition> getFullConditionSetFromShardingConditions(ApplicationLoadBalancer<ShardingKey> loadBalancer,
            Collection<RuleCondition> shardRuleConditions) throws InterruptedException, ExecutionException {
        if (shardRuleConditions.size() > ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE - NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE) {
            throw new IllegalArgumentException("Too many shardRuleConditions for a Rule. One ShardRule can contain a maximum of " + (ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE - NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE) + " Path conditions because we need" +
                    NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE + " for matching these rules to a replicaset!");
        }
        final Collection<RuleCondition> ruleConditions = new ArrayList<>();
        // add extra rules
        ruleConditions.addAll(shardRuleConditions);
        ruleConditions.add(RuleCondition.builder().field("http-header")
                .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                        .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                .build());
        ruleConditions.add(loadBalancer.createHostHeaderRuleCondition(replicaSet.getHostname()));
       return ruleConditions;
    }
    
    /**
     * This function constructs a number of new rules for given {@code ruleConditions}. They are constructed by creating
     * rules and filling them with those conditions and the needed sharding (hostname and forward to replica) rules. And
     * the forward to {@code targetGroup} gets also added.
     * 
     * @param ruleConditions
     *            are all condition, e.g. "paths", which are planned to be inserted.
     * @param alb
     *            automatic loadBalancer where these rules are planned to be added
     * @param targetGroup
     *            targetGroup where the forward of the rule points to
     * @return all created rules which still need to be fed into the loadBalancer
     * @throws Exception
     */
    protected Iterable<Rule> addNewRulesFromPathConditions(Iterable<RuleCondition> ruleConditions,
            ApplicationLoadBalancer<ShardingKey> alb, TargetGroup<ShardingKey> targetGroup) throws Exception {
        final ArrayList<Rule> rules = new ArrayList<>();
        final int ruleIdx = alb.getFirstShardingPriority(replicaSet.getHostname());
        final Set<RuleCondition> ruleConditionsForConsumption = new HashSet<>();
        Util.addAll(ruleConditions, ruleConditionsForConsumption);
        ArrayList<software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule.Builder> ruleBuilders = ShardingRuleManagementHelper.getNewRulesBuildersForShardingKeys()
        while (!ruleConditionsForConsumption.isEmpty()) {
            LoadBalancerRuleInserter.create(alb, ApplicationLoadBalancer.MAX_PRIORITY,
                    ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER).shiftRulesToMakeSpaceAt(ruleIdx, 1);
            final Collection<RuleCondition> conditionsForNextRule = new ArrayList<>();
            for (final Iterator<RuleCondition> i = ruleConditionsForConsumption.iterator(); conditionsForNextRule
                    .size() < ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE
                            - NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE
                    && i.hasNext();) {
                conditionsForNextRule.add(i.next());
                i.remove();
            }
            // Build rule with new conditions
            rules.add(Rule.builder().priority("" + ruleIdx).conditions(conditionsForNextRule)
                    .actions(Action.builder()
                            .forwardConfig(ForwardActionConfig.builder()
                                    .targetGroups(TargetGroupTuple.builder()
                                            .targetGroupArn(targetGroup.getTargetGroupArn()).build())
                                    .build())
                            .type(ActionTypeEnum.FORWARD).build())
                    .build());
        }
        return alb.addRules(Util.toArray(rules, new Rule[0]));
    }
//        
    protected Iterable<Rule> addShardingRules(ApplicationLoadBalancer<ShardingKey> alb, Iterable<ShardingKey> shardingKeys,
            TargetGroup<ShardingKey> targetGroup) throws Exception {
        final int ruleIdx = alb.getFirstShardingPriority(replicaSet.getHostname());
        // change ALB rules to new ones
        // construct all conditions for all shardingkeys because one sharding key needs more than one condition
        Iterable<Rule.Builder> ruleBuilders = ShardingRuleManagementHelper.getNewRulesBuildersForShardingKeys(shardingKeys);
        ArrayList<Rule> rulesToAdd = new ArrayList<>();
        for (Rule.Builder builder : ruleBuilders) {
            LoadBalancerRuleInserter.create(alb, ApplicationLoadBalancer.MAX_PRIORITY,
                    ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER).shiftRulesToMakeSpaceAt(ruleIdx, 1);
            // finish rule with priority and action
            rulesToAdd.add(builder.priority("" + ruleIdx)
                    .actions(Action.builder()
                            .forwardConfig(ForwardActionConfig.builder()
                                    .targetGroups(TargetGroupTuple.builder()
                                            .targetGroupArn(targetGroup.getTargetGroupArn()).build())
                                    .build())
                            .type(ActionTypeEnum.FORWARD).build())
                    .build());
        }
        return alb.addRules(Util.toArray(rulesToAdd, new Rule[0]));
    }

    /**
     * Search through all load balancers and returns the first load balancer with enough rules left for
     * the new sharding keys + the replicaSet's rules. When no load balancer is found, a new one gets created. The replicaSet gets moved to the new load balancer.
     */
    protected ApplicationLoadBalancer<ShardingKey> getFreeLoadBalancerAndMoveReplicaSet() throws Exception {
        int existingShardingRules = 0;
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> s : replicaSet.getShards().entrySet()) {
            existingShardingRules = existingShardingRules + Util.size(s.getKey().getRules());
        }
        // shardingKeys holds those keys to add/initially create (the remove case doesn't get here);
        // hence we have to add the rules required for those keys to the existing rules
        // FIXME shouldn't we compute the number of required rules for the new entire set of sharding keys? What if rules were not / no longer fully filled with conditions? -->cleanup
        final int requiredRules = numberOfRequiredRules(Util.size(shardingKeys))
                + (existingShardingRules + /* 5 std rules per replica set */ NUMBER_OF_RULES_PER_REPLICA_SET);
        final ApplicationLoadBalancer<ShardingKey> res;
        if (Util.size(replicaSet.getLoadBalancer().getRules())
                + numberOfRequiredRules(Util.size(shardingKeys)) < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER) {
            res = replicaSet.getLoadBalancer();
        } else {
            // Another loadbalancer
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers = getLandscape()
                    .getLoadBalancers(region);
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancersFiltered = Util.filter(loadBalancers,
                    t -> {
                        try {
                            return t.getVpcId().equals(replicaSet.getLoadBalancer().getVpcId())
                                    && !t.getArn().equals(replicaSet.getLoadBalancer().getArn());
                        } catch (InterruptedException | ExecutionException e) {
                            logger.log(Level.WARNING, "Exception while trying to obtain a load balancer's ARN", e);
                            throw new RuntimeException(e);
                        }
                    });
            final ApplicationLoadBalancer<ShardingKey> alb = getDNSLoadbalancerWithRulesLeft(loadBalancersFiltered,
                    requiredRules + /* 5 default rules for the replica set */ NUMBER_OF_RULES_PER_REPLICA_SET);
            if (alb != null) {
                // There is an alb left with enough rules
                res = alb;
            } else {
                final Set<String> loadBalancerNames = new HashSet<>();
                for (ApplicationLoadBalancer<ShardingKey> lb : loadBalancers) {
                    loadBalancerNames.add(lb.getName());
                }
                final String name = getAvailableDNSMappedAlbName(loadBalancerNames);
                // Create a new alb
                res = getLandscape().createLoadBalancer(name, region, getSecurityGroupForVpc());
            }
            changeReplicaSetLoadBalancer(res, replicaSet);
        }
        return res;
    }
    
    private SecurityGroup getSecurityGroupForVpc() throws InterruptedException, ExecutionException {
        return getLandscape().getSecurityGroup(replicaSet.getLoadBalancer().getSecurityGroupIds().get(0), region);
    }

    /**
     * This method changes the {@code replicaSetToMove}'s load balancer to another one. This method contains a 6min
     * sleep, which is necessary for ensuring that all DNS rules point to the new one. This method can fail if the
     * target group names are too long because of the {@link TargetGroup#TEMP_SUFFIX} suffix for temporary target
     * groups.
     * 
     * @param targetAlb
     *            application load balancer to move to
     * @param replicaSetToMove
     *            replica set to move
     */
    private void changeReplicaSetLoadBalancer(ApplicationLoadBalancer<ShardingKey> targetAlb,
            AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSetToMove) throws Exception {
        // Move replicaset to this alb with all shards
        // create temporary targetgroups
        final Collection<TargetGroup<ShardingKey>> tempTargetGroups = new ArrayList<>();
        final Collection<TargetGroup<ShardingKey>> originalTargetGroups = new ArrayList<>();
        final Map<TargetGroup<ShardingKey>, Iterable<ShardingKey>> shardingKeysPerTargetGroup = new HashMap<>();
        final Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups = new HashMap<>();
        final Map<AwsShard<ShardingKey>, TargetGroup<ShardingKey>> shardToTempTargetGroup = new HashMap<>();
        // add non sharding rules for replicaset
        final Collection<Rule> tempRules = new ArrayList<>();
        createTargetGroupsForMoving(shardToTempTargetGroup, tempTargetGroups, replicaSetToMove, targetGroupsToTempTargetgroups, originalTargetGroups, shardingKeysPerTargetGroup);
        addRulesForMoving(targetAlb, shardToTempTargetGroup, tempRules, replicaSetToMove, targetGroupsToTempTargetgroups, originalTargetGroups, shardingKeysPerTargetGroup);
        // set new DNS record -> overwrites old entry
        final String hostname = replicaSetToMove.getHostname();
        getLandscape().setDNSRecordToApplicationLoadBalancer(replicaSetToMove.getHostedZoneId(),
                hostname, targetAlb, /* force */ true);
        // wait until new DNS record is alive
        for (int i = 0; i < 6; i++) {
            Thread.sleep(AwsLandscape.DEFAULT_DNS_TTL_SECONDS * /* conversion seconds to ms */ 1000);
            logger.info(()->("Still waiting for DNS record " + hostname));
        }
        logger.info(()->("Done waiting for DNS record " + hostname));
        // remove all old rules pointing to original TargetGroups
        final Collection<Rule> rulesToRemove = new ArrayList<>();
        replicaSetToMove.getLoadBalancer().getRulesForTargetGroups(originalTargetGroups)
                .forEach(t -> rulesToRemove.add(t));
        rulesToRemove.add(replicaSetToMove.getDefaultRedirectRule());
        getLandscape().deleteLoadBalancerListenerRules(region, rulesToRemove.toArray(new Rule[0]));
        for (Entry<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> entry : targetGroupsToTempTargetgroups
                .entrySet()) {
            targetAlb.replaceTargetGroupInForwardRules(entry.getValue(), entry.getKey());
        }
        for (TargetGroup<ShardingKey> t : tempTargetGroups) {
            getLandscape().deleteTargetGroup(t);
        }
    }
    
    private void createTargetGroupsForMoving(
            Map<AwsShard<ShardingKey>, TargetGroup<ShardingKey>> shardToTempTargetGroup,
            Collection<TargetGroup<ShardingKey>> tempTargetGroups,
            AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSetToMove,
            Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups,
            Collection<TargetGroup<ShardingKey>> originalTargetGroups,
            Map<TargetGroup<ShardingKey>, Iterable<ShardingKey>> shardingKeysPerTargetGroup) throws Exception {
        final TargetGroup<ShardingKey> targetgroupMasterTemp = getLandscape()
                .copyTargetGroup(replicaSetToMove.getMasterTargetGroup(), TargetGroup.TEMP_SUFFIX);
        final TargetGroup<ShardingKey> targetgroupPublicTemp = getLandscape()
                .copyTargetGroup(replicaSetToMove.getPublicTargetGroup(), TargetGroup.TEMP_SUFFIX);
        tempTargetGroups.add(targetgroupMasterTemp);
        tempTargetGroups.add(targetgroupPublicTemp);
        targetGroupsToTempTargetgroups.put(replicaSetToMove.getMasterTargetGroup(), targetgroupMasterTemp);
        targetGroupsToTempTargetgroups.put(replicaSetToMove.getPublicTargetGroup(), targetgroupPublicTemp);
        originalTargetGroups.add(replicaSetToMove.getMasterTargetGroup());
        originalTargetGroups.add(replicaSetToMove.getPublicTargetGroup());
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> shardAndShardingKeys : replicaSetToMove.getShards().entrySet()) {
            final TargetGroup<ShardingKey> tempShardTargetGroup = getLandscape()
                    .copyTargetGroup(shardAndShardingKeys.getKey().getTargetGroup(), TargetGroup.TEMP_SUFFIX);
            shardToTempTargetGroup.put(shardAndShardingKeys.getKey(), tempShardTargetGroup);
            shardingKeysPerTargetGroup.put(shardAndShardingKeys.getKey().getTargetGroup(), shardAndShardingKeys.getValue());
            tempTargetGroups.add(tempShardTargetGroup);
            originalTargetGroups.add(shardAndShardingKeys.getKey().getTargetGroup());
            targetGroupsToTempTargetgroups.put(shardAndShardingKeys.getKey().getTargetGroup(), tempShardTargetGroup);
        }
    }

    private void addRulesForMoving(ApplicationLoadBalancer<ShardingKey> targetAlb,
            Map<AwsShard<ShardingKey>, TargetGroup<ShardingKey>> shardToTempTargetGroup, Collection<Rule> tempRules,
            AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSetToMove,
            Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups,
            Collection<TargetGroup<ShardingKey>> originalTargetGroups,
            Map<TargetGroup<ShardingKey>, Iterable<ShardingKey>> shardingKeysPerTargetGroup) throws Exception {
        targetAlb
                .addRulesAssigningUnusedPriorities(/* forceContiguous */ true,
                        Optional.empty(), createRules(targetAlb, replicaSet.getHostname(),
                                targetGroupsToTempTargetgroups.get(replicaSetToMove.getMasterTargetGroup()),
                                targetGroupsToTempTargetgroups.get(replicaSetToMove.getPublicTargetGroup())))
                .forEach(t -> tempRules.add(t));
        for (final Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> shardAndShardingKeys : replicaSetToMove.getShards().entrySet()) {
            addShardingRules(targetAlb, shardingKeysPerTargetGroup.get(shardAndShardingKeys.getKey().getTargetGroup()),
                    shardToTempTargetGroup.get(shardAndShardingKeys.getKey())).forEach(t -> tempRules.add(t)); 
        }
    }
    
    protected int numberOfRequiredRules(int numberOfShardingKeys) {
        return (int) (numberOfShardingKeys * ShardingRulePathConditionBuilder.numberOfShardConditionsPerShard() / (ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE-NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE))
                + (int) Math.signum(/* one more because casting to int rounds down */ numberOfShardingKeys %
                        (ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE-NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE));
    }

    /**
     * Returns a DNS-Mapped load balancer with enough rules left 
     * @param loadBalancers
     *          list of all load balancers to search through.
     * @param numberOfRules
     *          number of required rules left in this load balancer
     * @return
     */        
    private ApplicationLoadBalancer<ShardingKey> getDNSLoadbalancerWithRulesLeft(
            Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers, int numberOfRules) {
        final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancersFiltered = Util.filter(loadBalancers,
                t -> t.getName().startsWith(ApplicationLoadBalancer.DNS_MAPPED_ALB_NAME_PREFIX));
        ApplicationLoadBalancer<ShardingKey> res = null;
        for (ApplicationLoadBalancer<ShardingKey> loadBalancer : loadBalancersFiltered) {
            if (Util.size(loadBalancer.getRules()) < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER - numberOfRules) {
                res = loadBalancer;
                break;
            }
        }
        return res;
    }

    /**
     * iterates through all numbers from {@link ApplicationLoadBalancer#MAX_PRIORITY} to 1 (lowest index) and checks if
     * any priority is not in the rule set. returns the first available priority. If no rules is available, it returns
     * -1;
     */
    protected int getHighestAvailableIndex(Iterable<Rule> rules) {
        for (int i = ApplicationLoadBalancer.MAX_PRIORITY; i > 1; i--) {
            String y = "" + i;
            if (!StreamSupport.stream(rules.spliterator(), false).anyMatch(t -> t.priority().contains(y))) {
                return i; // return priority if there was no rule with the same
            }
        }
        return -1; // if no free priority was found
    }

    /**
     * Picks a new load balancer name following the pattern {@link #DNS_MAPPED_ALB_NAME_PREFIX}{@code [0-9]+} that is
     * not part of {@code loadBalancerNames} and has the least number.
     */
    private String getAvailableDNSMappedAlbName(Set<String> loadBalancerNames) {
        final Set<Integer> numbersTaken = new HashSet<>();
        for (final String loadBalancerName : loadBalancerNames) {
            final Matcher matcher = ApplicationLoadBalancer.ALB_NAME_PATTERN.matcher(loadBalancerName);
            if (matcher.find()) {
                numbersTaken.add(Integer.parseInt(matcher.group(1)));
            }
        }
        return ApplicationLoadBalancer.DNS_MAPPED_ALB_NAME_PREFIX
                + IntStream.range(0, ApplicationLoadBalancer.MAX_ALBS_PER_REGION).filter(i -> !numbersTaken.contains(i))
                        .min().getAsInt();
    }

    // this silently assumes that a String casts into a ShardingKey without problems
    @SuppressWarnings("unchecked")
    public static <ShardingKey> ShardingKey getShardingKeyFromPathCondition(String path) {
        return (ShardingKey) ShardingRulePathConditionBuilder.getShardingKeyFromCondition(path);
    }

}
