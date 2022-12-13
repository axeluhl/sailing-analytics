package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
    static final String SHARD_SUFFIX = "-S";
    static final String TEMP_TARGETGROUP_SUFFIX = "-TMP";
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
        final boolean ret;
        final Iterable<TargetGroup<ShardingKey>> targetGroups = getLandscape().getTargetGroups(region);
        if (Util.size(Util.filter(targetGroups, t -> t.getName().equals(name))) != 0) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }

    protected Collection<Rule> addShardingRules(ApplicationLoadBalancer<ShardingKey> alb, Set<String> shardingkeys,
            TargetGroup<ShardingKey> targetgroup) throws Exception {
        // change ALB rules to new ones
        final Collection<Rule> rules = new ArrayList<Rule>();
        final Set<String> shardingKeyForConsumption = new HashSet<>();
        shardingKeyForConsumption.addAll(shardingkeys);
        final int ruleIdx = alb.getFirstShardingPriority(replicaSet.getHostname());
        while (!shardingKeyForConsumption.isEmpty()) {// first make space at priority 1 <- highest priority
            alb.shiftRulesToMakeSpaceAt(ruleIdx);
            final Collection<RuleCondition> conditions = convertShardingKeysToConditions(shardingKeyForConsumption);
            conditions.add(RuleCondition.builder().field("http-header")
                    .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                            .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                    .build());
            alb.addRules(Rule.builder().priority("" + ruleIdx).conditions(conditions)
                    .actions(Action.builder()
                            .forwardConfig(ForwardActionConfig.builder()
                                    .targetGroups(TargetGroupTuple.builder()
                                            .targetGroupArn(targetgroup.getTargetGroupArn()).build())
                                    .build())
                            .type(ActionTypeEnum.FORWARD).build())
                    .build()).forEach(t -> rules.add(t));
        }
        return rules;
    }

    protected ApplicationLoadBalancer<ShardingKey> getFreeLoadbalancerAndMoveReplicaset() throws Exception {
        final int requiredRules = numberOfRequiredRules(Util.size(shardingKeys)
                + (replicaSet.getShards().size() + /* 5 std rules per replicaset */NUMBER_OF_RULES_PER_REPLICA_SET)
                        * /* for guaranteeing availability */2);
        final ApplicationLoadBalancer<ShardingKey> res;
        if (ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER
                - Util.size(replicaSet.getLoadBalancer().getRules()) > requiredRules) {
            // if the replicaset's loadbalancer has enough free rules left
            res = replicaSet.getLoadBalancer();
        } else {
            // Another loadbalancer
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadBalancers = getLandscape()
                    .getLoadBalancers(region);
            ApplicationLoadBalancer<ShardingKey> alb = getLoadbalancerWithRulesLeft(loadBalancers,
                    requiredRules + /* 5 default rules for the replica set */NUMBER_OF_RULES_PER_REPLICA_SET);
            if (alb != null) {
                // There is an alb left with enough rules
                res = alb;
            } else {
                final Set<String> loadBalancerNames = new HashSet<>();
                for (ApplicationLoadBalancer<ShardingKey> lb : loadBalancers) {
                    loadBalancerNames.add(lb.getName());
                }
                String name = getAvailableDNSMappedAlbName(loadBalancerNames);
                // Create a new alb
                alb = getLandscape().createLoadBalancer(name, region);
                res = alb;
            }
            changeReplicaSetLoadBalancer(alb, replicaSet);
        }
        return res;
    }
    /**
     * This functions changes the {@code replicaSetToMode}'s load balancer to another one.
     * This function contains a 6min sleep, which is necessary for ensuring that all DNS rules point to the new one. 
     * This function can fail is the target group names are to long because of the {@code TargetGroup.TEMP_SUFFIX} suffix for temporary target groups.
     * @param alb
     *          application load balancer to move to
     * @param replicaSetToMove
     *          replica set to move
     */
    private void changeReplicaSetLoadBalancer(ApplicationLoadBalancer<ShardingKey> alb,
            AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSetToMove) throws Exception {
        // Move replicaset to this alb with all shards
        // create temporary targetgroups
        final TargetGroup<ShardingKey> targetMaster = replicaSetToMove.getMasterTargetGroup();
        final TargetGroup<ShardingKey> targetPublic = replicaSetToMove.getPublicTargetGroup();
        final TargetGroup<ShardingKey> targetgroupMasterTemp = getLandscape()
                .copyTargetGroup(replicaSetToMove.getMasterTargetGroup(), TargetGroup.TEMP_SUFFIX);
        final TargetGroup<ShardingKey> targetgroupPublicTemp = getLandscape()
                .copyTargetGroup(replicaSetToMove.getPublicTargetGroup(), TargetGroup.TEMP_SUFFIX);
        final Collection<TargetGroup<ShardingKey>> temptargetgroups = new ArrayList<>();
        final Collection<TargetGroup<ShardingKey>> originaltargetgroups = new ArrayList<>();
        final Map<TargetGroup<ShardingKey>, Set<String>> keysAssignment = new HashMap<>();
        final Map<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> targetGroupsToTempTargetgroups = new HashMap<>();
        temptargetgroups.add(targetgroupMasterTemp);
        temptargetgroups.add(targetgroupPublicTemp);
        targetGroupsToTempTargetgroups.put(targetMaster, targetgroupMasterTemp);
        targetGroupsToTempTargetgroups.put(targetPublic, targetgroupPublicTemp);
        originaltargetgroups.add(targetMaster);
        originaltargetgroups.add(targetPublic);
        // add rules from replicaset
        final Collection<Rule> tempRules = new ArrayList<Rule>();
        alb.addRulesAssigningUnusedPriorities(true,
                createRules(alb, targetgroupMasterTemp, targetgroupPublicTemp, true)).forEach(t -> tempRules.add(t));
        // For each shard in replicaset -> move
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> shard : replicaSetToMove.getShards().entrySet()) {
            final TargetGroup<ShardingKey> tempShardTargetGroup = getLandscape()
                    .copyTargetGroup(shard.getKey().getTargetGroup(), TargetGroup.TEMP_SUFFIX);
            final Set<String> s = new HashSet<>();
            for (ShardingKey key : shard.getValue()) {
                s.add(key.toString());
            }
            keysAssignment.put(shard.getKey().getTargetGroup(), s);
            temptargetgroups.add(tempShardTargetGroup);
            addShardingRules(alb, s, tempShardTargetGroup).forEach(t -> tempRules.add(t));
            originaltargetgroups.add(shard.getKey().getTargetGroup());
            targetGroupsToTempTargetgroups.put(shard.getKey().getTargetGroup(), tempShardTargetGroup);
        }
        // set new DNS record -> overwrites old entry
        getLandscape().setDNSRecordToApplicationLoadBalancer(replicaSetToMove.getHostedZoneId(),
                replicaSetToMove.getHostname(), alb, /* force */ true);
        // wait until new DNS record is alive
        for (int i = 0; i < 6; i++) {
            Thread.sleep(AwsLandscape.DEFAULT_DNS_TTL_SECONDS * /* conversion seconds to ms */ 1000);
            logger.info("Still waiting.");
        }
        // remove all old rules pointing to original TargetGroups
        final Collection<Rule> rulesToRemove = new ArrayList<>();
        replicaSetToMove.getLoadBalancer().getRulesForTargetGroups(originaltargetgroups)
                .forEach(t -> rulesToRemove.add(t));
        ;
        rulesToRemove.add(replicaSetToMove.getDefaultRedirectRule());
        getLandscape().deleteLoadBalancerListenerRules(region, rulesToRemove.toArray(new Rule[0]));
        for (Entry<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> entry : targetGroupsToTempTargetgroups
                .entrySet()) {
            alb.replaceTargetGroupInForwardRules(entry.getValue(), entry.getKey());
        }
        for (TargetGroup<ShardingKey> t : temptargetgroups) {
            getLandscape().deleteTargetGroup(t);
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

    private ApplicationLoadBalancer<ShardingKey> getLoadbalancerWithRulesLeft(
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

    private static Collection<RuleCondition> convertShardingKeysToConditions(Set<String> shardingkeys) {
        final Collection<RuleCondition> res = new ArrayList<RuleCondition>();
        final Collection<String> remove = new ArrayList<String>();
        final Iterator<String> iter = shardingkeys.iterator();
        int idx = 0;
        while (idx < 3 && iter.hasNext()) {
            String shardingkey = iter.next();
            remove.add(shardingkey);
            idx++;
        }
        res.add(RuleCondition.builder().field("path-pattern").pathPatternConfig(hhcb -> hhcb.values(remove)).build());
        shardingkeys.removeAll(remove);
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
