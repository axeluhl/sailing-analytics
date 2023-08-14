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
    static final int maxWildcardsPerRule = 6;
    static public <ShardingKey> Iterable<RuleCondition>  getConditionsForShardingKey(ShardingKey shardingkey) throws Exception{
        return new ShardingRulePathConditionBuilder<>().ShardingKey(shardingkey).build();
    }
    
    static <ShardingKey> ArrayList<RuleCondition> getNewConditionsForShardingKeys(Iterable<ShardingKey> shardingkeys) throws Exception{
        final ArrayList<RuleCondition> conditions = new ArrayList<>();
        for (ShardingKey k : shardingkeys) {
            conditions.addAll((new ShardingRulePathConditionBuilder<>().ShardingKey(k).build()));
        }
        return conditions;
    }
    
    static public <ShardingKey> Iterable<Builder> getNewRulesBuildersForShardingKeys(Iterable<ShardingKey> shardingKeys) throws Exception {
        ArrayList<RuleCondition> conditions = getNewConditionsForShardingKeys(shardingKeys);
        ArrayList<Builder> rules = new ArrayList<>();
        
        while( !conditions.isEmpty()) {
            ArrayList<RuleCondition> conditionsforOneRule = new ArrayList<RuleCondition>();
            while(countNrOfWildcards(conditionsforOneRule) <= maxWildcardsPerRule && conditionsforOneRule.size() <ApplicationLoadBalancer.MAX_CONDITIONS_PER_RULE
                    - ShardProcedure.NUMBER_OF_STANDARD_CONDITIONS_FOR_SHARDING_RULE) {
                
            }
            rules.add(Rule.builder().conditions(conditionsforOneRule));
        }
        return rules;
    }
    
    
    
    static public int countNrOfWildcards(ArrayList<RuleCondition> conditions) {
        int count = 0;
        for(RuleCondition condition : conditions) {
            for(String path : condition.pathPatternConfig().values()) {
                count += ShardingRulePathConditionBuilder.countOfWildcards(path);
            }
        }
        return count;
    }
}
