package com.sap.sse.landscape.aws.impl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.sap.sse.common.Builder;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
public class ShardingRulePathConditionBuilder<ShardingKey> implements Builder<ShardingRulePathConditionBuilder<ShardingKey>,Collection<RuleCondition>>{
    
    // two dimensional array with keys [patterns][beforeKey(0)/Afterkey(1)]
     private static final String[][] patterns = {
            {"gwt/*/leaderboard/", ""},                 //0
            {"*/api/*/v?/leaderboards/",""},            //1
            {"*/api/*/v?/leaderboards/", "/*"},         //2
            {"*/api/*/v?/regattas/", ""},               //3
            {"*/api/*/v?/regattas/","/*"}               //4
    };
    public static int numberOfShardConditionsPerShard() {
        return patterns.length;
    }
    
    public static String unsedPathConditionValue = "gwt/*/leaderboard/lauycaluy3cla3yrclaurlIYQL8";
    
    
    public static String getShardingKeyFromCondition(String condition) {
        for (int i = 0;i < patterns.length; i++) {
            if (condition.startsWith(patterns[i][0]) && condition.endsWith(patterns[i][1])) {
                // found correct pattern. Now adapting to every pattern.
                switch(i) {
                case 0:
                case 1:
                case 3:
                    return condition.substring(condition.lastIndexOf('/') + 1);
                case 2:
                case 4:
                    int idxLastSlash = condition.lastIndexOf('/');
                    return condition.substring(condition.lastIndexOf('/', idxLastSlash - 1) + 1, condition.lastIndexOf('/'));
                default:
                    throw new IllegalArgumentException(condition + " matches pattern " + i + " but there is no case for extracting the Shardingkey it!");
                }
            }
        }
        throw new IllegalArgumentException("In " + condition + " could be no shardingkey be found.");
    }
    
    private ShardingKey shardingKey;
    
    public ShardingRulePathConditionBuilder<ShardingKey> ShardingKey(ShardingKey key) {
        shardingKey = key;
        return this;
    }

    @Override
    public Collection<RuleCondition> build() throws Exception {
        final Collection<RuleCondition> ruleConditions = new ArrayList<>();
        final Collection<String> paths = getPathsForShardingKey(shardingKey);
        ruleConditions.add(
                RuleCondition.builder().field("path-pattern").values(paths).build());
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
        for (String[] i : patterns) {
            c.add(i[0] + shardingKey + i[1]);
        }
        return c;
    }
    
   
}
