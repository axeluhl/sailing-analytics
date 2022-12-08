package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import com.sap.sse.common.Duration;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.ShardNameDTO;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;
import com.sap.sse.shared.util.Wait;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ForwardActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthStateEnum;

public class CreateShard<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends AbstractAwsProcedureImpl<ShardingKey> {
    private static final Logger logger = Logger.getLogger(CreateShard.class.getName());
    static final String SHARD_SUFFIX = "-S";
    static final String TEMP_TARGETGROUP_SUFFIX = "-TP";
    final static int NUMBER_OF_RULES_PER_REPLICA_SET = 5;
    final static int MAX_RULES_PER_LOADBALANCER = 100;
    final String shardName;
    final Set<String> shardingKeys;
    final AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet;
    final Region region;
    final byte[] passphraseForPrivateKeyDecryption;
    final Mode selectedMode;

    protected CreateShard(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        this.shardName = builder.getShardName();
        this.passphraseForPrivateKeyDecryption = builder.getPassphrase();
        this.replicaSet = builder.getReplicaSet();
        this.shardingKeys = builder.getShardingKeys();
        this.region = builder.getRegion();
        this.selectedMode = builder.getSelectedMode() == null ? Mode.CREATE : builder.getSelectedMode(); // defaults to
                                                                                                         // create

    }

    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>, T extends CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends AbstractAwsProcedureImpl.Builder<BuilderT, T, ShardingKey> {
        BuilderT setShardName(String name);

        BuilderT setLandscape(AwsLandscape<String> landscape);

        BuilderT setShardingkeys(Set<String> shardingkeys);

        BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset);

        BuilderT setRegion(Region region);

        BuilderT setPassphrase(byte[] passphrase);

        BuilderT setMode(Mode mode);
    }

    static class BuilderImpl<BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends
            AbstractAwsProcedureImpl.BuilderImpl<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey>
            implements
            Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {
        private String shardName;
        private Set<String> shardingkeys;
        private AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset;
        private Region region;
        private byte[] passphraseForPrivateKeyDecryption;
        private Mode selectedMode;

        @Override
        public CreateShard<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            assert shardingkeys != null;
            assert replicaset != null;
            assert region != null;
            assert passphraseForPrivateKeyDecryption != null;

            return new CreateShard<ShardingKey, MetricsT, ProcessT>(this);

        }

        @Override
        public BuilderT setShardName(String name) {
            this.shardName = name;
            return self();
        }

        @Override
        public BuilderT setShardingkeys(Set<String> shardingkeys) {
            this.shardingkeys = shardingkeys;
            return self();
        }

        @Override
        public BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset) {
            this.replicaset = replicaset;
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

        public byte[] getPassphrase() {
            return passphraseForPrivateKeyDecryption;
        }

        public Region region() {
            return region;
        }

        public AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> getReplicaSet() {
            return replicaset;
        }

        public Set<String> getShardingKeys() {
            return shardingkeys;
        }

        public String getShardName() {
            return shardName;
        }

        public Region getRegion() {
            return region;
        }

        @Override
        public BuilderT setMode(Mode mode) {
            this.selectedMode = mode;
            return self();
        }

        public Mode getSelectedMode() {
            return selectedMode;
        }
    }

    public static enum Mode {
        CREATE, APPEND_SHARDINGKEY, REMOVE_SHARDINGKEY
    }

    @Override
    public void run() throws Exception {
        switch (selectedMode) {
        case APPEND_SHARDINGKEY:
            APPEND_SHARDINGKEY();
            break;
        case CREATE:
            CREATE();
            break;
        case REMOVE_SHARDINGKEY:
            REMOVE_SHARDINGKEY();
        default:
            break;
        }
    }

    private void CREATE() throws Exception {

        final ShardNameDTO name;
        if (shardName == null) {
            throw new Exception("Shardname is null, please enter a name");
        } else {
            name = replicaSet.getNextShardName(shardName);
        }
        if (!isTargetgroupNameUnique(name.getTargetgroupName())) {
            throw new Exception(
                    "targetgroup name with this shardname is not unique. You may change the last or first two chars");
        }
        ApplicationLoadBalancer<ShardingKey> loadbalancer = getFreeLoadbalancerAndMoveReplicaset();
        logger.info(
                "Creating Targer group for Shard " + name + ". Inheriting from Replicaset: " + replicaSet.getName());
        TargetGroup<ShardingKey> targetgroup = getLandscape().createTargetGroupWithoutLoadbalancer(region,
                name.getTargetgroupName(), replicaSet.getMaster().getPort());
        getLandscape().addTargetGroupTag(targetgroup.getTargetGroupArn(), ShardNameDTO.TAG_KEY, name.getName(), region);
        AwsAutoScalingGroup autoscalinggroup = replicaSet.getAutoScalingGroup();
        logger.info("Creating Autoscalinggroup for Shard " + shardName + ". Inheriting from Autoscalinggroup: "
                + autoscalinggroup.getName());
        getLandscape().createAutoscalinggroupFromExisting(autoscalinggroup, shardName, targetgroup, Optional.empty());
        // create one rules to random path for linking ALB to Targetgroup.
        if (loadbalancer != null) {
            Iterable<Rule> rules = loadbalancer.getRules();
            if (Util.size(rules) < MAX_RULES_PER_LOADBALANCER - getLenRequiredRules(Util.size(shardingKeys))) {
                int rulePrio = getHighestAvailableIndex(rules);
                if (rulePrio > 0) {
                    Rule newRule = Rule.builder().priority("" + rulePrio)
                            .conditions(
                                    RuleCondition.builder().field("http-header")
                                            .httpHeaderConfig(hhcb -> hhcb
                                                    .httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                                    .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA
                                                            .getB()))
                                            .build(),
                                    RuleCondition.builder().field("path-pattern")
                                            .pathPatternConfig(ppc -> ppc.values(/* just any path */"/temp/")).build())
                            .actions(Action.builder()
                                    .forwardConfig(ForwardActionConfig.builder()
                                            .targetGroups(TargetGroupTuple.builder()
                                                    .targetGroupArn(targetgroup.getTargetGroupArn()).build())
                                            .build())
                                    .type(ActionTypeEnum.FORWARD).build())
                            .build();
                    Iterable<Rule> newRuleSet = loadbalancer.addRules(newRule);
                    // put the scaling policy
                    getLandscape().putScalingPolicy(autoscalinggroup, shardName, targetgroup, loadbalancer);
                    // wait until instances are running
                    Wait.wait(new Callable<Boolean>() {
                        public Boolean call() {
                            boolean ret = true;
                            Map<AwsInstance<ShardingKey>, TargetHealth> healths = getLandscape()
                                    .getTargetHealthDescriptions(targetgroup);
                            if (healths.isEmpty()) {
                                ret = false; // if there is no Aws in target
                            } else {
                                for (Map.Entry<AwsInstance<ShardingKey>, TargetHealth> instance : healths.entrySet()) {
                                    if (instance.getValue().state() != TargetHealthStateEnum.HEALTHY) {
                                        ret = false; // if this instance is unhealthy
                                        break;
                                    }
                                }
                            }
                            return ret;
                        }
                    }, Optional.of(Duration.ONE_MINUTE.times(10)), Duration.ONE_SECOND.times(5), Level.INFO,
                            "Instances not yet healty");
                    // remove dummy-rule
                    for (Rule r : newRuleSet) {
                        loadbalancer.deleteRules(r);
                    }
                    // change ALB rules to new ones
                    addShardingRules(loadbalancer, shardingKeys, targetgroup);
                } else {
                    throw new Exception("Unexpected Error - No prio left?");
                }
            } else {
                throw new Exception("Unexpected Error - Loadbalancer was null!");
            }
        }
    }

    private void APPEND_SHARDINGKEY() throws Exception {
        // get shard, targetgroup and loadbalancer
        AwsShard<ShardingKey> shard = null;
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> entry : replicaSet.getShards().entrySet()) {
            if (entry.getKey().getName().equals(shardName)) {
                shard = entry.getKey();
                break;
            }
        }
        if (shard == null) {
            throw new Exception("Shard not found!");
        }
        List<String> manipulatableShardingKeys = new ArrayList<>(); // list for manupulation -> elements are allowed to
                                                                    // be removed
        manipulatableShardingKeys.addAll(shardingKeys);
        TargetGroup<ShardingKey> targetgroup = shard.getTargetGroup();
        ApplicationLoadBalancer<ShardingKey> loadbalancer = shard.getLoadbalancer();
        Collection<TargetGroup<ShardingKey>> t = new ArrayList<>();
        t.add(targetgroup);
        // check if there is a rule left with space
        for (Rule r : shard.getRules()) {
            boolean updateRule = false;
            ArrayList<String> keys = new ArrayList<>();
            for (RuleCondition con : r.conditions()) {
                if (con.pathPatternConfig() != null) {
                    keys.addAll(con.values());
                }
            }
            while (keys.size() < ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE
                    - /* two conditions are required for host and forward to replica */2
                    && !manipulatableShardingKeys.isEmpty()) {
                keys.add(manipulatableShardingKeys.get(0));
                manipulatableShardingKeys.remove(0);
                updateRule = true;
            }
            ArrayList<RuleCondition> rulecon = new ArrayList<>();
            rulecon.add(
                    RuleCondition.builder().field("path-pattern").pathPatternConfig(hhcb -> hhcb.values(keys)).build());
            rulecon.add(RuleCondition.builder().field("http-header")
                    .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                            .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                    .build());
            Rule newRule = Rule.builder().ruleArn(r.ruleArn()).conditions(rulecon).build();
            if (updateRule) {
                getLandscape().modifyRuleConditions(region, newRule);
            }
        }
        if (!manipulatableShardingKeys.isEmpty()) {
            // check number of rules
            if (Util.size(loadbalancer.getRules())
                    + getLenRequiredRules(Util.size(shardingKeys)) < MAX_RULES_PER_LOADBALANCER) {
                // enough rules
                Set<String> keysCopy = new HashSet<>();
                keysCopy.addAll(shardingKeys);
                addShardingRules(loadbalancer, keysCopy, targetgroup);
            } else {
                // not enough rules
                ApplicationLoadBalancer<ShardingKey> alb = getFreeLoadbalancerAndMoveReplicaset();
                // set new rules
                Set<String> keysCopy = new HashSet<>();
                keysCopy.addAll(shardingKeys);
                addShardingRules(alb, keysCopy, targetgroup);
            }
        }
    }

    private void REMOVE_SHARDINGKEY() throws Exception {
        // get shard, targetgroup and loadbalancer
        AwsShard<ShardingKey> shard = null;
        for (Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> entry : replicaSet.getShards().entrySet()) {
            if (entry.getKey().getName().equals(shardName)) {
                shard = entry.getKey();
                break;
            }
        }
        if (shard == null) {
            throw new Exception("Shard not found!");
        }
        // remove conditions in rules where path is the sharding key
        Set<String> shardingKeysFromConditions = new HashSet<>();
        for (Rule r : shard.getRules()) {
            for (RuleCondition condition : r.conditions()) {
                if (condition.pathPatternConfig() != null) {
                    shardingKeysFromConditions.addAll(condition.values());
                }
            }
            ;
        }
        getLandscape().deleteLoadBalancerListenerRules(region, Util.toArray(shard.getRules(), new Rule[0]));

        shardingKeysFromConditions = Util.asSet(Util.filter(shardingKeysFromConditions, t -> {
            for (String s : shardingKeys) {
                if (s.equals(t)) {
                    return false;
                }

            }
            return true;
        }));
        // change ALB rules to new ones
        addShardingRules(shard.getLoadbalancer(), Util.asSet(shardingKeysFromConditions), shard.getTargetGroup());
    }

    private boolean isTargetgroupNameUnique(String name) {
        Iterable<TargetGroup<ShardingKey>> targetgroups = getLandscape().getTargetGroups(region);
        for (TargetGroup<ShardingKey> t : targetgroups) {
            if (name.equals(t.getName())) {
                return false;
            }
        }
        return true;
    }

    private Collection<Rule> addShardingRules(ApplicationLoadBalancer<ShardingKey> alb, Set<String> shardingkeys,
            TargetGroup<ShardingKey> targetgroup) throws Exception {
        // change ALB rules to new ones
        final Collection<Rule> rules = new ArrayList<Rule>();
        final Set<String> shardingKeyForConsumption = new HashSet<>();
        shardingKeyForConsumption.addAll(shardingkeys);
        final int ruleIdx = alb.getFirstPriorityOfHostname(replicaSet.getHostname());
        while (!shardingKeyForConsumption.isEmpty()) {// first make space at prio 1 <- highest prio
            alb.shiftRulesToMakeSpaceAt(ruleIdx);
            Collection<RuleCondition> conditions = convertShardingKeysToConditions(shardingKeyForConsumption);
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
            ;
        }
        return rules;
    }

    private ApplicationLoadBalancer<ShardingKey> getFreeLoadbalancerAndMoveReplicaset() throws Exception {
        final int requiredRules = getLenRequiredRules(Util.size(shardingKeys)
                + (replicaSet.getShards().size() + /* 5 std rules per replicaset */NUMBER_OF_RULES_PER_REPLICA_SET)
                        * /* for guaranteeing availability */2);
        final ApplicationLoadBalancer<ShardingKey> res;
        if (ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER
                - Util.size(replicaSet.getLoadBalancer().getRules()) > requiredRules) {
            // if the replicaset's loadbalancer has enough free rules left
            res = replicaSet.getLoadBalancer();
        } else {
            // Another loadbalancer
            final Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancers = getLandscape()
                    .getLoadBalancers(region);
            ApplicationLoadBalancer<ShardingKey> alb = getLoadbalancerWithRulesLeft(loadbalancers,
                    requiredRules + /* 5 default rules for the replicaset */NUMBER_OF_RULES_PER_REPLICA_SET);
            if (alb != null) {
                // There is an alb left with enough rules
                res = alb;
            } else {
                Set<String> loadbalancerNames = new HashSet<>();

                for (ApplicationLoadBalancer<ShardingKey> lb : loadbalancers) {
                    loadbalancerNames.add(lb.getName());
                }
                String name = getAvailableDNSMappedAlbName(loadbalancerNames);
                // Create a new alb

                alb = getLandscape().createLoadBalancer(name, region);
                res = alb;
            }
            moveReplicaSeToLoadbalancer(alb, replicaSet);
        }
        return res;
    }

    private void moveReplicaSeToLoadbalancer(ApplicationLoadBalancer<ShardingKey> alb,
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
        ;
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
        // set new dns record -> overwrites old entry
        getLandscape().setDNSRecordToApplicationLoadBalancer(replicaSetToMove.getHostedZoneId(),
                replicaSetToMove.getHostname(), alb, /* force */ true);
        // wait until new dns record is alive
        for (int i = 0; i < 6; i++) {
            Thread.sleep(getLandscape().getDNSTTLInSeconds() * /* conversion seconds to ms */ 1000);
            logger.info("Still waiting.");
        }

        // remove all old rules pointing to original TargetGroups
        Collection<Rule> rulesToRemove = replicaSetToMove.getLoadBalancer()
                .getRulesForTargetGroups(originaltargetgroups);
        rulesToRemove.add(replicaSetToMove.getDefaultRedirectRule());
        getLandscape().deleteLoadBalancerListenerRules(region, rulesToRemove.toArray(new Rule[0]));
        for (Entry<TargetGroup<ShardingKey>, TargetGroup<ShardingKey>> entry : targetGroupsToTempTargetgroups
                .entrySet()) {
            alb.replaceTargetgroupInForwardRules(entry.getValue(), entry.getKey());
        }
        for (TargetGroup<ShardingKey> t : temptargetgroups) {
            getLandscape().deleteTargetGroup(t);
        }
    }

    private int getLenRequiredRules(int lenLeaderboards) {
        return (int) (lenLeaderboards / 3) + 1;
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
            Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancers, int rules) {
        final Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancersFIltered = Util.filter(loadbalancers,
                t -> t.getName().startsWith(ApplicationLoadBalancer.DNS_MAPPED_ALB_NAME_PREFIX));
        ApplicationLoadBalancer<ShardingKey> res = null;
        for (ApplicationLoadBalancer<ShardingKey> loadbalancer : loadbalancersFIltered) {
            if (Util.size(loadbalancer.getRules()) < 100 - rules) {
                res = loadbalancer;
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

    // iterates through all numbers from 999 ( highest index ) to 1 (lowest index) and checks if any priority is not in
    // the ruleset.
    // returns the first available priority. If no rules is available, it returns -1;
    private int getHighestAvailableIndex(Iterable<Rule> rules) {
        for (int i = 999; i > 1; i--) {
            String y = "" + i;
            if (StreamSupport.stream(rules.spliterator(), false).anyMatch(t -> {
                return (t.priority().contains(y));
            })) {
            } else {
                return i; // return prio if there was no rule with the same
            }
        }
        return -1; // if no free prio was found
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

    public static <MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}
