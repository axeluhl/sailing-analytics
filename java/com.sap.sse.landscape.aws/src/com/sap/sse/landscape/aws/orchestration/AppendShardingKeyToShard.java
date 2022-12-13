package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
/**
 * This class is supposed to append {@code shardingKeys} to the shard, identified by {@code shardName} from {@replicaSet}.
 * This is done by adding rule conditions to the shard's rule set and after that just appending new rules. This could lead to moving to replicaset to antoher
 * load balancer, because it may get full with new sharding rules.
 * @author I569653
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public class AppendShardingKeyToShard<ShardingKey, 
    MetricsT extends ApplicationProcessMetrics, 
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends ShardProcedure<ShardingKey, MetricsT, ProcessT> {

    public AppendShardingKeyToShard(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
    }

    static class BuilderImpl<BuilderT extends Builder<BuilderT, AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, 
        ShardingKey, 
        MetricsT extends ApplicationProcessMetrics, 
        ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends
        ShardProcedure.BuilderImpl<BuilderT, AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {

        @Override
        public AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            assert shardingKeys != null;
            assert replicaSet != null;
            assert region != null;
            assert passphraseForPrivateKeyDecryption != null;
            return new AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT>(this);
        }
    }

    @Override
    public void run() throws Exception {
        AwsShard<ShardingKey> shard = null;
        for (final Entry<AwsShard<ShardingKey>, Iterable<ShardingKey>> entry : replicaSet.getShards().entrySet()) {
            if (entry.getKey().getName().equals(shardName)) {
                shard = entry.getKey();
                break;
            }
        }
        if (shard == null) {
            throw new Exception("Shard not found!");
        }
        // list for manipulation -> elements are allowed to be removed!!
        final List<String> manipulatableShardingKeys = new ArrayList<>();
        manipulatableShardingKeys.addAll(shardingKeys);
        final TargetGroup<ShardingKey> targetgroup = shard.getTargetGroup();
        final ApplicationLoadBalancer<ShardingKey> loadBalancer = shard.getLoadbalancer();
        final Collection<TargetGroup<ShardingKey>> t = new ArrayList<>();
        t.add(targetgroup);
        // check if there is a rule left with space
        for (Rule r : shard.getRules()) {
            boolean updateRule = false;
            final ArrayList<String> keys = new ArrayList<>();
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
            final ArrayList<RuleCondition> rulecon = new ArrayList<>();
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
            if (Util.size(loadBalancer.getRules()) + numberOfRequiredRules(
                    Util.size(shardingKeys)) < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER) {
                // enough rules
                final Set<String> keysCopy = new HashSet<>();
                keysCopy.addAll(shardingKeys);
                addShardingRules(loadBalancer, keysCopy, targetgroup);
            } else {
                // not enough rules
                final ApplicationLoadBalancer<ShardingKey> alb = getFreeLoadbalancerAndMoveReplicaset();
                // set new rules
                final Set<String> keysCopy = new HashSet<>();
                keysCopy.addAll(shardingKeys);
                addShardingRules(alb, keysCopy, targetgroup);
            }
        }
    }

    public static <MetricsT extends ApplicationProcessMetrics, 
        ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, 
        BuilderT extends Builder<BuilderT, AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, 
        ShardingKey> 
        Builder<BuilderT, AppendShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}