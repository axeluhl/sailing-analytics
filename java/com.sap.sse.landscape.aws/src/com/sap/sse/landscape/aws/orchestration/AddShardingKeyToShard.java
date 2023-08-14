package com.sap.sse.landscape.aws.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.impl.ShardingRulePathConditionBuilder;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

/**
 * This procedure appends {@code shardingKeys} to a shard, identified by {@link #shardName} from
 * the {@link #replicaSet}. This is done by adding rule conditions to the shard's rule set and after that just appending new
 * rules. This could lead to moving to replica set to another load balancer, because it may get full with new sharding
 * rules.
 * 
 * @author I569653
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <ProcessT>
 */
public class AddShardingKeyToShard<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
        extends ShardProcedure<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(AddShardingKeyToShard.class.getName());

    public AddShardingKeyToShard(BuilderImpl<?, ShardingKey, MetricsT, ProcessT> builder) throws Exception {
        super(builder);
    }

    static class BuilderImpl<BuilderT extends Builder<BuilderT, AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
            extends
            ShardProcedure.BuilderImpl<BuilderT, AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> {

        @Override
        public AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT> build() throws Exception {
            assert shardingKeys != null;
            assert replicaSet != null;
            assert region != null;
            return new AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT>(this);
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
            throw new Exception("Shard "+shardName+" not found in replica set "+replicaSet.getName());
        }
        logger.info("Appending " + Util.joinStrings(", ", shardingKeys) + " to " + shardName);
        final TargetGroup<ShardingKey> targetgroup = shard.getTargetGroup();
        final ApplicationLoadBalancer<ShardingKey> loadBalancer = shard.getLoadBalancer();
        // building every condition for every shardingkey
        ArrayList<RuleCondition> ruleConditionToBeInserted = new ArrayList<>();
        for (ShardingKey k : shardingKeys) {
            ruleConditionToBeInserted.addAll((new ShardingRulePathConditionBuilder<>().ShardingKey(k).build()));
        }
        
        // check if there is a rule with space left for one or more additional conditions:
        for (Rule r : shard.getRules()) {
            boolean updateRule = false;
            boolean ruleIsEmpty = true;
            ArrayList<RuleCondition> conditionsForModThisRule = new ArrayList<>();
            for (RuleCondition con : r.conditions()) {
                // if we find a 
                if (con.pathPatternConfig() != null && Util.size(Util.filter(con.values(), t -> getShardingKeyFromPathCondition(t) != SHARDING_KEY_UNUSED_BY_ANY_APPLICATION)) != 0) {
                    // for rebuilding the conditions later, we need to add already existing conditions.
                    conditionsForModThisRule.add(con);
                    // if there are paths which are not SHARDING_KEY_UNUSED_BY_ANY_APPLICATION, than the rule is not empty.
                    ruleIsEmpty = false;
                }
            }
            if (ruleIsEmpty) {
                // the rule probably only has PATH_UNUSED_BY_ANY_APPLICATION and was a proxy rule, probably at the end of the list; remove
                loadBalancer.deleteRules(r);
            } else { // update only non-empty rule because we assume it won't be at the end of the list
                while (conditionsForModThisRule.size() < ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE - NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE
                        && !ruleConditionToBeInserted.isEmpty()) {
                    conditionsForModThisRule.add(ruleConditionToBeInserted.get(0));
                    ruleConditionToBeInserted.remove(0); 
                    updateRule = true;
                }
                final Collection<RuleCondition> ruleConditions = getFullConditionSetFromShardingConditions(loadBalancer, conditionsForModThisRule);
                // construct a rule only for transporting the conditions; no forwarding target is required for modifyRuleConditions
                Rule proxyRuleWithNewConditions = Rule.builder().ruleArn(r.ruleArn()).conditions(ruleConditions).build();
                if (updateRule) {
                    getLandscape().modifyRuleConditions(region, proxyRuleWithNewConditions);
                }
            }
        }
        if (!ruleConditionToBeInserted.isEmpty()) {
            // check number of rules
            if (Util.size(loadBalancer.getRules()) + numberOfRequiredRules(Util.size(shardingKeys))
                    < ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER) {
                // enough rules
                addNewRulesFromPathConditions(ruleConditionToBeInserted, loadBalancer, targetgroup);
            } else {
                // not enough rules
                final ApplicationLoadBalancer<ShardingKey> alb = getFreeLoadBalancerAndMoveReplicaSet();
                // set new rules
                addNewRulesFromPathConditions(ruleConditionToBeInserted, alb, targetgroup);
            }
        }
    }

    public static <MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, BuilderT extends Builder<BuilderT, AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT>, ShardingKey> Builder<BuilderT, AddShardingKeyToShard<ShardingKey, MetricsT, ProcessT>, ShardingKey, MetricsT, ProcessT> builder() {
        return new BuilderImpl<BuilderT, ShardingKey, MetricsT, ProcessT>();
    }
}