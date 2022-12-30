package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;

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
        extends AbstractAwsProcedureImpl<ShardingKey> {
    private static final Logger logger = Logger.getLogger(ShardProcedure.class.getName());
    final static int NUMBER_OF_RULES_PER_REPLICA_SET = 5;
    final static int NUMBER_OF_STANDARD_RULES_FOR_SHARDING_RULE = 2;
    protected final String shardName;
    final Set<String> shardingKeys;
    final AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet;
    final Region region;
    final byte[] passphraseForPrivateKeyDecryption;

    protected ShardProcedure(BuilderImpl<?,?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        this.shardName = builder.getShardName();
        this.passphraseForPrivateKeyDecryption = builder.getPassphrase();
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
        BuilderT setShardName(String name);

        BuilderT setLandscape(AwsLandscape<String> landscape);

        BuilderT setShardingkeys(Set<String> shardingkeys);

        BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset);

        BuilderT setRegion(Region region);

        BuilderT setPassphrase(byte[] passphrase);
    }

    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>, 
        T extends ShardProcedure<ShardingKey,MetricsT,ProcessT>,
        ShardingKey, 
        MetricsT extends ApplicationProcessMetrics, 
        ProcessT extends ApplicationProcess<ShardingKey, 
        MetricsT, ProcessT>>
            extends
            AbstractAwsProcedureImpl.BuilderImpl<BuilderT, T, ShardingKey>
            implements
            Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        protected String shardName;
        protected Set<String> shardingKeys;
        protected AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet;
        protected Region region;
        protected byte[] passphraseForPrivateKeyDecryption;

        @Override
        public BuilderT setShardName(String name) {
            this.shardName = name;
            return self();
        }

        @Override
        public BuilderT setShardingkeys(Set<String> shardingkeys) {
            this.shardingKeys = shardingkeys;
            return self();
        }

        @Override
        public BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset) {
            this.replicaSet = replicaset;
            return self();
        }

        @Override
        public BuilderT setRegion(Region region) {
            this.region = region;
            return self();
        }

        @Override
        public BuilderT setPassphrase(byte[] passphrase) {
            this.passphraseForPrivateKeyDecryption = passphrase;
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

        byte[] getPassphrase() {
            return passphraseForPrivateKeyDecryption;
        }

        Region region() {
            return region;
        }

        AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> getReplicaSet() {
            return replicaSet;
        }

        Set<String> getShardingKeys() {
            return shardingKeys;
        }

        String getShardName() {
            return shardName;
        }

        Region getRegion() {
            return region;
        }
    }

    protected boolean isTargetgroupNameUnique(String name) {
        final Iterable<TargetGroup<ShardingKey>> targetGroups = getLandscape().getTargetGroups(region);
        return Util.isEmpty(Util.filter(targetGroups, t -> t.getName().equals(name)));   
    }

    protected Iterable<Rule> addShardingRules(ApplicationLoadBalancer<ShardingKey> alb, Set<String> shardingKeys,
            TargetGroup<ShardingKey> targetGroup) throws Exception {
        // change ALB rules to new ones
        final Collection<Rule> rules = new ArrayList<Rule>();
        final Set<String> shardingKeyForConsumption = new HashSet<>();
        shardingKeyForConsumption.addAll(shardingKeys);
        final int ruleIdx = alb.getFirstShardingPriority(replicaSet.getHostname());
        while (!shardingKeyForConsumption.isEmpty()) {
            alb.shiftRulesToMakeSpaceAt(ruleIdx);
            final Collection<RuleCondition> conditions = convertShardingKeysToConditions(shardingKeyForConsumption);
            conditions.add(RuleCondition.builder().field("http-header")
                    .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                            .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                    .build());
            rules.add(Rule.builder().priority("" + ruleIdx).conditions(conditions)
                    .actions(Action.builder()
                            .forwardConfig(ForwardActionConfig.builder()
                                    .targetGroups(TargetGroupTuple.builder()
                                            .targetGroupArn(targetGroup.getTargetGroupArn()).build())
                                    .build())
                            .type(ActionTypeEnum.FORWARD).build())
                    .build());
        }
        return alb.addRules(Util.toArray(rules, new Rule[1]));
    }

    /**
     * Search through all load balancers and returns the first load balancer with enough rules left for
     * the new sharding keys + the replicaSet's rules. When no load balancer is found, a new one gets created. The replicaSet gets moved to the new load balancer.
     */
    protected ApplicationLoadBalancer<ShardingKey> getFreeLoadbalancerAndMoveReplicaset() throws Exception {
        int existingShardingRules = 0;
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> s : replicaSet.getShards().entrySet()) {
            existingShardingRules = existingShardingRules + Util.size(s.getKey().getRules());
        }
        final int requiredRules = numberOfRequiredRules(Util.size(shardingKeys))
                + (existingShardingRules + /* 5 std rules per replicaset */NUMBER_OF_RULES_PER_REPLICA_SET);
        final ApplicationLoadBalancer<ShardingKey> res;
        if (Util.size(replicaSet.getLoadBalancerRules())
                + numberOfRequiredRules(Util.size(shardingKeys)) < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER) {
            res = replicaSet.getLoadBalancer();
        } else {
            // Another loadbalancer
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers = getLandscape()
                    .getLoadBalancers(region);
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancersFiltered = Util.filter(loadBalancers,
                    t -> {
                        try {
                            return !t.getArn().equals(replicaSet.getLoadBalancer().getArn());
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
            final ApplicationLoadBalancer<ShardingKey> alb = getDNSLoadbalancerWithRulesLeft(loadBalancersFiltered,
                    requiredRules + /* 5 default rules for the replica set */NUMBER_OF_RULES_PER_REPLICA_SET);
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
                res = getLandscape().createLoadBalancer(name, region);
            }
            changeReplicaSetLoadBalancer(res, replicaSet);
        }

        return res;
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
        final Map<TargetGroup<ShardingKey>, Set<String>> keysAssignment = new HashMap<>();
        final Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups = new HashMap<>();
        final Map<AwsShard<ShardingKey>, TargetGroup<ShardingKey>> shardToTempTargetGroup = new HashMap<>();
        // add non sharding rules for replicaset
        final Collection<Rule> tempRules = new ArrayList<>();
        createTargetGroupsForMoving(shardToTempTargetGroup, tempTargetGroups, replicaSetToMove, targetGroupsToTempTargetgroups, originalTargetGroups, keysAssignment);
        addRulesForMoving(targetAlb, shardToTempTargetGroup, tempRules, replicaSetToMove, targetGroupsToTempTargetgroups, originalTargetGroups, keysAssignment);
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
            Map<TargetGroup<ShardingKey>, Set<String>> keysAssignment) throws Exception {
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
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> shard : replicaSetToMove.getShards().entrySet()) {
            final TargetGroup<ShardingKey> tempShardTargetGroup = getLandscape()
                    .copyTargetGroup(shard.getKey().getTargetGroup(), TargetGroup.TEMP_SUFFIX);
            final Set<String> s = new HashSet<>();
            for (ShardingKey key : shard.getValue()) {
                s.add(key.toString());
            }
            shardToTempTargetGroup.put(shard.getKey(), tempShardTargetGroup);
            keysAssignment.put(shard.getKey().getTargetGroup(), s);
            tempTargetGroups.add(tempShardTargetGroup);
            originalTargetGroups.add(shard.getKey().getTargetGroup());
            targetGroupsToTempTargetgroups.put(shard.getKey().getTargetGroup(), tempShardTargetGroup);
        }
    }

    private void addRulesForMoving(ApplicationLoadBalancer<ShardingKey> targetAlb,
            Map<AwsShard<ShardingKey>, TargetGroup<ShardingKey>> shardToTempTargetGroup, Collection<Rule> tempRules,
            AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSetToMove,
            Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups,
            Collection<TargetGroup<ShardingKey>> originalTargetGroups,
            Map<TargetGroup<ShardingKey>, Set<String>> keysAssignment) throws Exception {
        targetAlb.addRulesAssigningUnusedPriorities(true,
                createRules(targetAlb, targetGroupsToTempTargetgroups.get(replicaSetToMove.getMasterTargetGroup()),
                        targetGroupsToTempTargetgroups.get(replicaSetToMove.getPublicTargetGroup()), true))
                .forEach(t -> tempRules.add(t));
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> shard : replicaSetToMove.getShards().entrySet()) {
            addShardingRules(targetAlb, keysAssignment.get(shard.getKey().getTargetGroup()),
                    shardToTempTargetGroup.get(shard.getKey())).forEach(t -> tempRules.add(t));
        }
    }
    
    

    protected int numberOfRequiredRules(int numberOfLeaderboards) {
        return (int) (numberOfLeaderboards / NUMBER_OF_RULES_PER_REPLICA_SET - NUMBER_OF_STANDARD_RULES_FOR_SHARDING_RULE)
                + /* one more because casting to int round off */ 1;
    }

    private Rule[] createRules(ApplicationLoadBalancer<ShardingKey> alb, TargetGroup<ShardingKey> masterTarget,
            TargetGroup<ShardingKey> publicTarget, boolean includeDefaultRedirect) throws Exception {
        final Rule[] rules = new Rule[NUMBER_OF_RULES_PER_REPLICA_SET - (includeDefaultRedirect ? 0 : 1)];
        int counter = 0;
        if (includeDefaultRedirect) {
            rules[counter++] = alb.getDefaultRedirectRule(getHostName(), new PlainRedirectDTO());
        }
        rules[counter++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header")
                        .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER.getB()))
                        .build(),
                alb.createHostHeaderRuleCondition(getHostName()))
                .actions(createForwardToTargetGroupAction(masterTarget)).build();
        rules[counter++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header")
                        .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                        .build(),
                alb.createHostHeaderRuleCondition(getHostName()))
                .actions(createForwardToTargetGroupAction(publicTarget)).build();
        rules[counter++] = Rule.builder()
                .conditions(
                        RuleCondition.builder().field("http-request-method")
                                .httpRequestMethodConfig(hrmcb -> hrmcb.values("GET")).build(),
                        alb.createHostHeaderRuleCondition(getHostName()))
                .actions(createForwardToTargetGroupAction(publicTarget)).build();
        rules[counter++] = Rule.builder().conditions(alb.createHostHeaderRuleCondition(getHostName()))
                .actions(createForwardToTargetGroupAction(masterTarget)).build();
        assert counter == NUMBER_OF_RULES_PER_REPLICA_SET - (includeDefaultRedirect ? 0 : 1);
        return rules;
    }

    private Action createForwardToTargetGroupAction(TargetGroup<ShardingKey> targetGroup) {
        return Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc -> fc
                .targetGroups(TargetGroupTuple.builder().targetGroupArn(targetGroup.getTargetGroupArn()).build()))
                .build();
    }

    private String getHostName() throws Exception {
        return replicaSet.getHostname();
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

    private static Collection<RuleCondition> convertShardingKeysToConditions(Set<String> shardingKeys) {
        final Collection<RuleCondition> res = new ArrayList<RuleCondition>();
        final Collection<String> remove = new ArrayList<String>();
        final Iterator<String> iter = shardingKeys.iterator();
        int idx = 0;
        while (idx < 3 && iter.hasNext()) {
            String shardingkey = iter.next();
            remove.add(shardingkey);
            idx++;
        }
        res.add(RuleCondition.builder().field("path-pattern").pathPatternConfig(hhcb -> hhcb.values(remove)).build());
        shardingKeys.removeAll(remove);
        return res;
    }

    // iterates through all numbers from {@code ApplicationLoadBalancer.MAX_PRIORITY} to 1 (lowest index) and checks if
    // any priority is not in
    // the rule set.
    // returns the first available priority. If no rules is available, it returns -1;
    protected int getHighestAvailableIndex(Iterable<Rule> rules) {
        for (int i = ApplicationLoadBalancer.MAX_PRIORITY; i > 1; i--) {
            String y = "" + i;
            if (StreamSupport.stream(rules.spliterator(), false).anyMatch(t -> {
                return (t.priority().contains(y));
            })) {
            } else {
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
}
