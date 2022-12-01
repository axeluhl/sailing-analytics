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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import com.google.gwt.thirdparty.guava.common.collect.Iterables;
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

public class CreateShard<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractAwsProcedureImpl<ShardingKey> {
    private static final Logger logger = Logger.getLogger(CreateShard.class.getName());
    static final String SHARD_SUFFIX  ="-S";
    static final String TEMP_TARGETGROUP_SUFFIX  ="-TP";
    protected static int NUMBER_OF_RULES_PER_REPLICA_SET = 5;
    final String shardName;
    final Set<String> shardingkeys;
    final AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset;
    final Region region;
    final byte[] passphraseForPrivateKeyDecryption;
    protected CreateShard(BuilderImpl<?,  ShardingKey,MetricsT, ProcessT> builder) throws Exception {
        super(builder);
        this.shardName = builder.getShardName();
        this.passphraseForPrivateKeyDecryption  =builder.getPassphrase();
        this.replicaset  =builder.getReplicaSet();
        this.shardingkeys = builder.getShardingKeys();
        this.region  =builder.getRegion();
        
    }
    
    public static interface Builder<BuilderT extends Builder<BuilderT,T, ShardingKey, MetricsT, ProcessT>,
    T extends CreateShard<ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends AbstractAwsProcedureImpl.Builder<BuilderT,T, ShardingKey> {
        BuilderT setShardName(String name);
        BuilderT setLandscape(AwsLandscape<String> landscape);
        BuilderT setShardingkeys(Set<String> shardingkeys);
        BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset);
        BuilderT setRegion(Region region);
        BuilderT setPassphrase(byte[] passphrase);
    }
    
    static class BuilderImpl<BuilderT extends Builder<BuilderT,CreateShard<ShardingKey, MetricsT, ProcessT>,ShardingKey, MetricsT, ProcessT>,
    ShardingKey, MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    extends AbstractAwsProcedureImpl.BuilderImpl<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey>
    implements Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey,MetricsT, ProcessT> {
        private String shardName;
        private Set<String> shardingkeys;
        private AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset;
        private Region region;
        private byte[] passphraseForPrivateKeyDecryption;
        
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
           this.shardingkeys  =shardingkeys;
           return self();
        }
        @Override
        public BuilderT setReplicaset(AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaset ) {
            this.replicaset = replicaset;
            return self();
        }

        @Override
        public BuilderT setRegion(Region region) {
            this.region  =region;
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
        
        public AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT>  getReplicaSet() {
            return replicaset;
        }
        
        public Set<String> getShardingKeys(){
            return shardingkeys;
        }
        
        public String getShardName() {
            return shardName;
        }
        
        public Region getRegion() {
            return region;
        }   
    }

    @Override
    public void run() throws Exception {
        
        final String name;
        if(shardName == null) {
            name  =replicaset.getNextShardName();
        } else {
            name = replicaset.getNextShardName(shardName);
        }
        ApplicationLoadBalancer<ShardingKey> loadbalancer = getFreeLoadbalancer();
        logger.info(
                "Creating Targer group for Shard " + name + ". Inheriting from Replicaset: " + replicaset.getName());
        TargetGroup<ShardingKey> targetgroup = getLandscape().createTargetGroupWithoutLoadbalancer(region, name,
                replicaset.getMaster().getPort());
        AwsAutoScalingGroup autoscalinggroup = replicaset.getAutoScalingGroup();
        logger.info("Creating Autoscalinggroup for Shard " + shardName + ". Inheriting from Autoscalinggroup: "
                + autoscalinggroup.getName());
        getLandscape().createAutoscalinggroupFromExisting(autoscalinggroup, shardName, targetgroup, Optional.empty());
        // create one rules to random path for linking ALB to Targetgroup.
        if (loadbalancer != null) {
            // TODO fix calculation of number of required rules
            Iterable<Rule> rules = loadbalancer.getRules();
            if (Iterables.size(rules) < /* Max rule count */ 30 - getLenRequiredRules(Iterables.size(shardingkeys))) { 
                int rulePrio = getHighestAvailableIndex(rules);
                if (rulePrio > 0) {
                    // logger.info("Creating testing rule with prio: " + rulePrio);
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
                                    // _logger.info("Health status: " + instance.getValue().state());
                                    if (instance.getValue().state() != TargetHealthStateEnum.HEALTHY) {
                                        ret = false; // if this instance is unhealthy
                                        break;
                                    }
                                }
                            }
                            return ret;
                        }
                    }, Optional.of(Duration.ONE_MINUTE.times(10)), Duration.ONE_SECOND.times(5), Level.INFO,
                            "Has no instances");

                    // remove dummy-rule
                    for (Rule r : newRuleSet) {
                        loadbalancer.deleteRules(r);
                    }
                    // change ALB rules to new ones
                    addShardingRules(loadbalancer, shardingkeys, targetgroup);
                } else {
                    throw new Exception("Unexpected Error - No prio left?");
                }
            } else {
                throw new Exception("Loadbalancer was null!");
            }
        }
    }
    
    
    private   Collection<Rule> addShardingRules(ApplicationLoadBalancer<ShardingKey> alb, Set<String> shardingkeys, TargetGroup<ShardingKey> targetgroup) throws Exception {
     // change ALB rules to new ones
        Collection<Rule> rules = new ArrayList<Rule>();
        Set<String> shardingKeysForConsumption = new HashSet<>();
        shardingKeysForConsumption.addAll(shardingkeys);
        int ruleIdx = alb.getFirstPriorityOfHostname(replicaset.getHostname());
        while (!shardingKeysForConsumption.isEmpty()) {// first make space at prio 1 <- highest prio
            alb.shiftRulesToMakeSpaceAt(ruleIdx);
            Collection<RuleCondition> conditions = convertShardingKeysToConditions(shardingKeysForConsumption);

            conditions
                    .add(RuleCondition.builder().field("http-header")
                            .httpHeaderConfig(hhcb -> hhcb
                                    .httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                    .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                            .build());
            alb
                    .addRules(Rule.builder().priority("" + ruleIdx).conditions(conditions)
                            .actions(Action.builder().forwardConfig(ForwardActionConfig.builder()
                                    .targetGroups(TargetGroupTuple.builder()
                                            .targetGroupArn(targetgroup.getTargetGroupArn()).build())
                                    .build()).type(ActionTypeEnum.FORWARD).build())
                            .build()).forEach(t -> rules.add(t));;
        }
        return rules;
    }
    
    private ApplicationLoadBalancer<ShardingKey> getFreeLoadbalancer() throws Exception {
        final int requiredRules = getLenRequiredRules(Iterables.size(shardingkeys) +replicaset.getShards().size());
        final ApplicationLoadBalancer<ShardingKey> res;
        if(ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER - Iterables.size(replicaset.getLoadBalancer().getRules()) > requiredRules ) {
            // if the replicaset's loadbalancer has enough free rules left
            res = replicaset.getLoadBalancer();
        } else {
            //Another loadbalancer 
            Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancers = getLandscape().getLoadBalancers(region);
            ApplicationLoadBalancer<ShardingKey>  alb = getLoadbalancerWithRulesLeft(loadbalancers, requiredRules + /* 5 default rules for the replicaset */NUMBER_OF_RULES_PER_REPLICA_SET);
            if(alb != null) {
                // There is an alb left with enough rules
                res = alb;
            } else {
                Set<String> loadbalancerNames  =new HashSet<>();
             
                for(ApplicationLoadBalancer<ShardingKey> lb  :loadbalancers) {
                    loadbalancerNames.add(lb.getName());
                }
                String name = getAvailableDNSMappedAlbName(loadbalancerNames);
                // Create a new alb
                
                alb = getLandscape().createLoadBalancer(name, region);
                res = alb;
            }
            moveReplicaSeToLoadbalancer(alb, replicaset);
        }
        return res;
    }
    
    private void moveReplicaSeToLoadbalancer(ApplicationLoadBalancer<ShardingKey> alb, AwsApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> replicaSet) throws Exception {
      //Move replicaset to this alb with all shards
        //create temporary targetgroups
        final TargetGroup<ShardingKey> targetMaster = replicaSet.getMasterTargetGroup();
        final TargetGroup<ShardingKey> targetPublic = replicaSet.getPublicTargetGroup();
        final TargetGroup<ShardingKey> targetgroupMasterTemp = getLandscape().copyTargetGroup(replicaSet.getMasterTargetGroup(), TEMP_TARGETGROUP_SUFFIX);
        final TargetGroup<ShardingKey> targetgroupPublicTemp = getLandscape().copyTargetGroup(replicaSet.getPublicTargetGroup(), TEMP_TARGETGROUP_SUFFIX);
        final Collection<TargetGroup<ShardingKey>> temptargetgroups = new ArrayList<>();
        final Collection<TargetGroup<ShardingKey>> originaltargetgroups = new ArrayList<>();
        final Map<TargetGroup<ShardingKey>,Set<String>> keysAssignment = new HashMap<>();
        temptargetgroups.add(targetgroupMasterTemp);
        temptargetgroups.add(targetgroupPublicTemp);
        originaltargetgroups.add(targetMaster);
        originaltargetgroups.add(targetPublic);
        // add rules from replicaset
        final Collection<Rule> tempRules = new ArrayList<Rule>();
        alb.addRulesAssigningUnusedPriorities(true,  createRules(alb, targetgroupMasterTemp,targetgroupPublicTemp, true)).forEach(t -> tempRules.add(t));;
        //For each shard in replicaset -> move
        for(Entry<Integer,AwsShard<ShardingKey>> shard : replicaSet.getShards().entrySet()) {
            final TargetGroup<ShardingKey> tempShardTargetGroup = getLandscape().copyTargetGroup(shard.getValue().getTargetGroup(), TEMP_TARGETGROUP_SUFFIX);                
            final Set<String> s = new HashSet<>();
            for(ShardingKey key : shard.getValue().getKeys()) {
                s.add(key.toString());
            }
            keysAssignment.put(shard.getValue().getTargetGroup(), s);
            temptargetgroups.add(tempShardTargetGroup);
            addShardingRules(alb,s,tempShardTargetGroup).forEach(t -> tempRules.add(t));
            originaltargetgroups.add(shard.getValue().getTargetGroup());
        }
        //set new dns record -> overwrites old entry
        getLandscape().setDNSRecordToApplicationLoadBalancer(replicaSet.getHostedZoneId(), replicaSet.getHostname(), alb,/* force */ true);
        //wait until new dns record is alive
        Thread.sleep(getLandscape().getDNSTTLInSeconds() */* 10 times*/ 10 * /* conversion seconds to ms */ 1000);
        // ThreadPoolUtil.INSTANCE.
        Collection<Rule> rulesToRemove  = replicaSet.getLoadBalancer().getRulesForTargetGroups(originaltargetgroups); //remove all old rules pointing to original targetgroups
        getLandscape().deleteLoadBalancerListenerRules(region, rulesToRemove.toArray(new Rule[0])); //remove rules in old alb
        //write rules to old targetgroups in new alb
        Collection<Rule> tempRulesToRemove  = alb.getRulesForTargetGroups(temptargetgroups);
        tempRulesToRemove.add(replicaSet.getDefaultRedirectRule());
        alb.addRulesAssigningUnusedPriorities(true, createRules(alb, targetMaster,targetPublic, true));
        for(Entry<TargetGroup<ShardingKey>,Set<String>> entry : keysAssignment.entrySet()) {
            addShardingRules(alb, entry.getValue(), entry.getKey());
        }
        // Remove temp rules
        getLandscape().deleteLoadBalancerListenerRules(region, tempRulesToRemove.toArray(new Rule[0])); // remove
                                                                                                        // rules in
                                                                                                        // old alb
        for (TargetGroup<ShardingKey> t : temptargetgroups) {
            getLandscape().deleteTargetGroup(t);
        }
    }
    
    private int getLenRequiredRules(int lenLeaderboards) {
        return (int) (lenLeaderboards /3) + 1;
    }
    
    private Rule[] createRules(ApplicationLoadBalancer<ShardingKey> alb, TargetGroup<ShardingKey> masterTarget, TargetGroup<ShardingKey> publicTarget, boolean includeDefaultRedirect) throws Exception{
        final Rule[] rules = new Rule[NUMBER_OF_RULES_PER_REPLICA_SET -(includeDefaultRedirect ? 0 :1)];
        int counter = 0;
        if(includeDefaultRedirect) {
           rules[counter++] = alb.getDefaultRedirectRule(getHostName(), new PlainRedirectDTO());
        }
        rules[counter++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header").httpHeaderConfig(hhcb->hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO).values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER.getB())).build(),
                alb.createHostHeaderRuleCondition(getHostName())).
                actions(createForwardToTargetGroupAction(masterTarget)).
                build();
        rules[counter++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header").httpHeaderConfig(hhcb->hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO).values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB())).build(),
                alb.createHostHeaderRuleCondition(getHostName())).
                actions(createForwardToTargetGroupAction(publicTarget)).
                build();
        rules[counter++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-request-method").httpRequestMethodConfig(hrmcb->hrmcb.values("GET")).build(),
                alb.createHostHeaderRuleCondition(getHostName())).
                actions(createForwardToTargetGroupAction(publicTarget)).
                build();
        rules[counter++] = Rule.builder().conditions(
                alb.createHostHeaderRuleCondition(getHostName())).
                actions(createForwardToTargetGroupAction(masterTarget)).
                build();
        
        assert counter == NUMBER_OF_RULES_PER_REPLICA_SET - (includeDefaultRedirect ? 0 :1);
        return rules;
    }
    
    private Action createForwardToTargetGroupAction(TargetGroup<ShardingKey> targetGroup) {
        return Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc -> fc.targetGroups(
                TargetGroupTuple.builder().targetGroupArn(targetGroup.getTargetGroupArn()).build())) .build();
    }
    
    private String getHostName() throws Exception{
        return replicaset.getHostname();
    }
    
    private ApplicationLoadBalancer<ShardingKey> getLoadbalancerWithRulesLeft(
            Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancers, int rules) {
        final Iterable<ApplicationLoadBalancer<ShardingKey>> loadbalancersFIltered = Util.filter(loadbalancers,
                t -> t.getName().startsWith("DNSMapped"));
        ApplicationLoadBalancer<ShardingKey> res = null;
        for (ApplicationLoadBalancer<ShardingKey> loadbalancer : loadbalancersFIltered) {
            if (Iterables.size(loadbalancer.getRules()) < 100 - rules) {
                res = loadbalancer;
                break;
            }
        }
        return res;
    }

    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
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

    public static 
    <MetricsT extends ApplicationProcessMetrics, 
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> 
    Builder<BuilderT,CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}
