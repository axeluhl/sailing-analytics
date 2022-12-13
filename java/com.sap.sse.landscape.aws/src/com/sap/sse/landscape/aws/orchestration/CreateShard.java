package com.sap.sse.landscape.aws.orchestration;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.ShardName;
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
/**
 * This class is for creating shards out of the {@code shardingKeys}. This class creats a target group, and an autoscaling group and inserts rules into the 
 * {@code replicaSet}'s load balancer. If the load balancer does not have enough rules left in it'S HTTPS-listener, the whole replica set gets moved to another load balancer.
 * @author I569653
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public class CreateShard<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends ShardProcedure<ShardingKey, MetricsT, ProcessT> {
    
    private static int DEFAULT_INSTANCE_STARTUP_TIME = 180;
    private static final Logger logger = Logger.getLogger(ShardProcedure.class.getName());

    public CreateShard(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
    }

    static class BuilderImpl<BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends
            ShardProcedure.BuilderImpl<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {

        @Override
        public CreateShard<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            assert shardingKeys != null;
            assert replicaSet != null;
            assert region != null;
            assert passphraseForPrivateKeyDecryption != null;
            return new CreateShard<ShardingKey, MetricsT, ProcessT>(this);
        }
    }

    @Override
    public void run() throws Exception {
        final ShardName name;
        if (shardName == null) {
            throw new Exception("Shardname is null, please enter a name");
        } else {
            name = replicaSet.getNewShardName(shardName);
        }
        if (!isTargetgroupNameUnique(name.getTargetgroupName())) {
            throw new Exception(
                    "targetgroup name with this shardname is not unique. You may change the last or first two chars");
        }
        final ApplicationLoadBalancer<ShardingKey> loadBalancer = getFreeLoadbalancerAndMoveReplicaset();
        logger.info(
                "Creating Targer group for Shard " + name + ". Inheriting from Replicaset: " + replicaSet.getName());
        final TargetGroup<ShardingKey> targetgroup = getLandscape().createTargetGroupWithoutLoadbalancer(region,
                name.getTargetgroupName(), replicaSet.getMaster().getPort());
        getLandscape().addTargetGroupTag(targetgroup.getTargetGroupArn(), ShardName.TAG_KEY, name.getName(), region);
        final AwsAutoScalingGroup autoScalingGroup = replicaSet.getAutoScalingGroup();
        logger.info("Creating Autoscalinggroup for Shard " + shardName + ". Inheriting from Autoscalinggroup: "
                + autoScalingGroup.getName());
        getLandscape().createAutoscalingGroupFromExisting(autoScalingGroup, shardName, targetgroup, Optional.empty());
        // create one rules to random path for linking ALB to Targetgroup.
        if (loadBalancer != null) {
            final Iterable<Rule> rules = loadBalancer.getRules();
            if (Util.size(rules) < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER
                    - numberOfRequiredRules(Util.size(shardingKeys))) {
                final int rulePrio = getHighestAvailableIndex(rules);
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
                    final Iterable<Rule> newRuleSet = loadBalancer.addRules(newRule);
                    getLandscape().putScalingPolicy(DEFAULT_INSTANCE_STARTUP_TIME, shardName, targetgroup,
                            AwsAutoScalingGroup.DEFAULT_MAX_REQUESTS_PER_TARGET, region);
                    // wait until instances are running
                    Wait.wait(new Callable<Boolean>() {
                        public Boolean call() {
                            boolean ret = true;
                            final Map<AwsInstance<ShardingKey>, TargetHealth> healths = getLandscape()
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
                        loadBalancer.deleteRules(r);
                    }
                    // change ALB rules to new ones
                    addShardingRules(loadBalancer, shardingKeys, targetgroup);
                } else {
                    throw new Exception("Unexpected Error - No prio left?");
                }
            } else {
                throw new Exception("Unexpected Error - Loadbalancer was null!");
            }
        }
    }

    public static <MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, BuilderT extends Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> Builder<BuilderT, CreateShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}