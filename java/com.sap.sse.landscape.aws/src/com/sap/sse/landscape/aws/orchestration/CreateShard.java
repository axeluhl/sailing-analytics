package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.stream.StreamSupport;
import com.google.gwt.thirdparty.guava.common.collect.Iterables;
import com.sap.sse.common.Duration;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;
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
    static final String SHARD_SUFFIX  ="-S";
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
            assert shardName != null;
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
        //logger.info("Creating Targer group for Shard "+shardName + ". Inheriting from Replicaset: " + replicaset.getName());
        
        String name = replicaset.getShardName();
        TargetGroup<ShardingKey> targetgroup  = getLandscape().createTargetGroupWithoutLoadbalancer(region, name, replicaset.getMaster().getPort());
        AwsAutoScalingGroup autoscalinggroup = replicaset.getAutoScalingGroup();      
        //logger.info("Creating Autoscalinggroup for Shard "+shardName + ". Inheriting from Autoscalinggroup: " + autoscalinggroup.getName());
        getLandscape().createAutoscalinggroupFromExisting(autoscalinggroup, shardName, targetgroup, Optional.empty());
        ApplicationLoadBalancer<ShardingKey> loadbalancer = replicaset.getLoadBalancer();
        Iterable<Rule> rules = loadbalancer.getRules();
        //create one rules to random path for linking ALB to Targetgroup.
        //TODO fix calculation of number of required rules
        if (Iterables.size(rules) < /* Max rule count */ 30 - shardingkeys.size() - /* 2 are necessary*/2) { // add rules to loadbalancer
            int rulePrio = getHighestAvailableIndex(rules);
            if (rulePrio > 0) {
                //logger.info("Creating testing rule with prio: " +  rulePrio);
                Rule newRule = Rule.builder().priority("" + rulePrio).conditions(
                        RuleCondition.builder().field("http-header")
                                .httpHeaderConfig(
                                        hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                                .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                                .build(),
                        RuleCondition.builder().field("path-pattern").pathPatternConfig(ppc -> ppc.values(/* just any path */"/temp/"))
                                .build())
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
                
                //wait until instances are running
                Wait.wait(new Callable<Boolean>() {
                    public Boolean call() {
                        boolean ret = true;
                        Map<AwsInstance<ShardingKey>, TargetHealth> healths = getLandscape().getTargetHealthDescriptions(targetgroup);
                        //final Logger _logger = Logger.getLogger(LandscapeManagementWriteServiceImpl.class.getName());
                        //_logger.info("Healths size: "  + healths.size());
                        if(healths.isEmpty()) {
                            ret = false; // if there is no Aws in target
                        }else {
                        for(Map.Entry<AwsInstance<ShardingKey>, TargetHealth> instance : healths.entrySet()) {
                            //_logger.info("Health status: "  + instance.getValue().state());
                           if(instance.getValue().state() != TargetHealthStateEnum.HEALTHY) {
                               ret = false; // if this instance is unhealthy
                               break;
                           }
                        }
                        }
                        return  ret ;
                        
                        
                    }
                },Optional.of(Duration.ONE_MINUTE.times(10)), Duration.ONE_SECOND.times(5), Level.INFO, "Has no instances");

                // remove dummy-rule
                for (Rule r : newRuleSet) {
                    loadbalancer.deleteRules(r);
                }

                // change ALB rules to new ones
                int ruleIdx = 3;
                while (!shardingkeys.isEmpty()) {// first make space at prio 1 <- highest prio
                    loadbalancer.shiftRulesToMakeSpaceAt(ruleIdx);
                    Collection<RuleCondition> conditions = getShardConditions(shardingkeys);
                    
                    conditions.add(RuleCondition.builder()
                                    .field("http-header")
                                    .httpHeaderConfig(hhcb -> hhcb
                                            .httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                    .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                            .build());
                    loadbalancer.addRules(Rule.builder()
                            .priority("" +ruleIdx)
                            .conditions(conditions)
                            .actions(Action.builder()
                                    .forwardConfig(ForwardActionConfig.builder()
                                            .targetGroups(TargetGroupTuple.builder()
                                                    .targetGroupArn(targetgroup.getTargetGroupArn()).build())
                                            .build())
                                    .type(ActionTypeEnum.FORWARD).build()).build());
                }

            } else {
                throw new Exception("No prio left?");
            }
        }else {
            throw new Exception("Writing rules to new ALB not yet implemented ");
        }
        
    }

    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return (AwsLandscape<ShardingKey>) super.getLandscape();
    }
    
    Collection<RuleCondition> getShardConditions(Set<String> shardingkeys){
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

    public static 
    <MetricsT extends ApplicationProcessMetrics, 
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> 
    Builder<BuilderT,CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}
