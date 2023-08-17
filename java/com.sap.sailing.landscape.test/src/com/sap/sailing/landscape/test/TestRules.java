package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.aws.impl.ShardingRuleManagementHelper;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;


public class TestRules {
    @Before
    public void setUp() {
        
    }
    
    @Test
    public void testCountWildcards() {
        int result = ShardingRuleManagementHelper.countNrOfWildcards(Collections.singletonList(RuleCondition.builder().pathPatternConfig(t -> t.values("?*/asd")).build()));
        assertEquals(2, result);
    }
    
    @Test
    public void testConditionsForShardingKeys() throws Exception {
        ArrayList<String> shardingKeys = new ArrayList<>();
        shardingKeys.add("Test");
        shardingKeys.add("Peter");
        Iterable<Rule> rules = ShardingRuleManagementHelper.getNewRulesBuildersForShardingKeys(shardingKeys);
        assertTrue(Util.size(rules) == 4 );
        for(Rule b : rules) {
            int result = ShardingRuleManagementHelper.countNrOfWildcards(b.conditions());
            assertTrue(result <= ShardingRuleManagementHelper.maxWildcardsPerRule);
        }
    }
}
