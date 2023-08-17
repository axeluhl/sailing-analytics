package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.orchestration.ShardProcedure;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule.Builder;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

/**
 * Helper class for managing sharding rules because there are only 6 wildcards allowed in one rule for conditions.
 * Returned Rules do not include an action or an priority
 */
public class ShardingRuleManagementHelper {
    static public final int maxWildcardsPerRule = 6;

    static public <ShardingKey> Iterable<RuleCondition> getConditionsForShardingKey(ShardingKey shardingkey)
            throws Exception {
        return new ShardingRulePathConditionBuilder<>().ShardingKey(shardingkey).build();
    }

    static <ShardingKey> ArrayList<RuleCondition> getNewConditionsForShardingKeys(Iterable<ShardingKey> shardingkeys)
            throws Exception {
        final ArrayList<RuleCondition> conditions = new ArrayList<>();
        for (ShardingKey k : shardingkeys) {
            conditions.addAll((new ShardingRulePathConditionBuilder<>().ShardingKey(k).build()));
        }
        return conditions;
    }

    static public <ShardingKey> Iterable<Rule> getNewRulesBuildersForShardingKeys(Iterable<ShardingKey> shardingKeys)
            throws Exception {
        ArrayList<RuleCondition> conditions = getNewConditionsForShardingKeys(shardingKeys);
        ArrayList<String> pathValues = new ArrayList<>();
        for (RuleCondition con : conditions) {
            pathValues.addAll(con.pathPatternConfig().values());
        }
        ArrayList<Rule> rules = new ArrayList<>();
        while (!pathValues.isEmpty()) {
            ArrayList<String> pathsForOneRule = new ArrayList<>();
            
            while (pathValues.size() > 0 && countNrOfWildcardsPaths(pathsForOneRule) + countNrOfWildcards(pathValues.get(0)) <= maxWildcardsPerRule
                    && pathsForOneRule.size() < ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE
                            - ShardProcedure.NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE) {
                pathsForOneRule.add(pathValues.remove(0));
            }
            RuleCondition.builder().pathPatternConfig(t -> t.values(pathsForOneRule).build()).build();
            rules.add(Rule.builder().conditions(RuleCondition.builder().pathPatternConfig(t -> t.values(pathsForOneRule).build()).build()).build());
        }
        return rules;
    }

    static public int countNrOfWildcards(Iterable<RuleCondition> conditions) {
        int count = 0;
        for (RuleCondition condition : conditions) {
            count += countNrOfWildcards(condition);
        }
        return count;
    }
    
    static public int countNrOfWildcardsPaths(Iterable<String> paths) {
        int count = 0;
        for (String condition : paths) {
            count += countNrOfWildcards(condition);
        }
        return count;

    }

    static public int countNrOfWildcards(RuleCondition condition) {
        int count = 0;
        for (String path : condition.pathPatternConfig().values()) {
            count += countNrOfWildcards(path);

        }
        return count;
    }
    
    static public int countNrOfWildcards(String path) {
        return ShardingRulePathConditionBuilder.countOfWildcards(path);

    }
}
