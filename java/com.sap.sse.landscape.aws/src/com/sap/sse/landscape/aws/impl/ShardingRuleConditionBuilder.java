package com.sap.sse.landscape.aws.impl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.sap.sse.common.Builder;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
public class ShardingRuleConditionBuilder<ShardingKey> implements Builder<ShardingRuleConditionBuilder<ShardingKey>,Collection<RuleCondition>>{
    
    // two dimensional array with keys [condition][beforeKey(0)/Afterkey(1)]
    static final String[][] construction = {
            {"*/leaderboard/", ""},
            {"*/v?/leaderboards/",""},
            {"*/v?/leaderboards/", "/*"},
            {"*/v?/regattas/", ""},
            {"*/v?/regattas/","/*"}
    };
    public static int numberOfShardConditionsPerShard() {
        return construction.length;
    }
    private ShardingKey shardingKey;
    
    public ShardingRuleConditionBuilder<ShardingKey> ShardingKey(ShardingKey key) {
        shardingKey = key;
        return this;
    }

    @Override
    public Collection<RuleCondition> build() throws Exception {
        final Collection<RuleCondition> ruleConditions = new ArrayList<>();
        final Collection<String> paths = getPathsForShardingKey(shardingKey);
        ruleConditions.add(
                RuleCondition.builder().field("path-pattern").pathPatternConfig(hhcb -> hhcb.values(paths)).build());
        return ruleConditions;
    }
    
    protected Collection<RuleCondition> getPathConditionsForOneShardingKey(ShardingKey shardingKey) throws InterruptedException, ExecutionException {
        final Collection<RuleCondition> ruleConditions = new ArrayList<>();
        final Collection<String> paths = getPathsForShardingKey(shardingKey);
        ruleConditions.add(
                RuleCondition.builder().field("path-pattern").pathPatternConfig(hhcb -> hhcb.values(paths)).build());
        return ruleConditions;
    }
    
    // returns every required path condition
    Collection<String> getPathsForShardingKey(ShardingKey shardingKey) {
        ArrayList<String> c = new ArrayList<>();
        for (String[] i : construction) {
            c.add(i[0] + shardingKey + i[1]);
        }
        return c;
    }
    
   
}
