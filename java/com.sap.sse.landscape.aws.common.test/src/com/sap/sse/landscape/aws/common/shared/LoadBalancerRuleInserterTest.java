package com.sap.sse.landscape.aws.common.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter;
import com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter.LoadBalancerAdapter;
import com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter.RuleAdapter;

public class LoadBalancerRuleInserterTest {
    private LoadBalancerAdapter<TestRuleAdapter> loadBalancerAdapter;
    private LoadBalancerRuleInserter<String, TestRuleAdapter> ruleInserter;

    private static class TestRuleAdapter implements RuleAdapter<TestRuleAdapter> {
        private final boolean isDefault;
        private final int priority;
        private final String ruleArn;
        
        public TestRuleAdapter(boolean isDefault, int priority, String ruleArn) {
            super();
            this.isDefault = isDefault;
            this.priority = priority;
            this.ruleArn = ruleArn;
        }

        @Override
        public boolean isDefault() {
            return isDefault;
        }

        @Override
        public String priority() {
            return ""+priority;
        }

        @Override
        public String ruleArn() {
            return ruleArn;
        }

        @Override
        public TestRuleAdapter copyWithNewPriority(int priorityToUseForRuleCopy) {
            return new TestRuleAdapter(isDefault, priorityToUseForRuleCopy, ruleArn);
        }
    }

    private static class TestLoadBalancerAdapter implements LoadBalancerAdapter<TestRuleAdapter> {
        private static final long serialVersionUID = -4853040303405813921L;
        private Iterable<TestRuleAdapter> rules;

        @Override
        public String getName() {
            return "Test Load Balancer Adapter";
        }

        @Override
        public Iterable<TestRuleAdapter> getRules() {
            return rules;
        }

        @Override
        public void updateLoadBalancerListenerRulePriorities(
                List<Pair<Integer, TestRuleAdapter>> newPrioritiesForExistingRules) {
            rules = Util.map(newPrioritiesForExistingRules, p -> p.getB().copyWithNewPriority(p.getA()));
        }

        @Override
        public void addRules(List<TestRuleAdapter> rulesToAdd) {
            rules = Util.concat(Arrays.asList(rules, rulesToAdd));
        }
    }

    @Before
    public void setUp() {
        loadBalancerAdapter = new TestLoadBalancerAdapter();
        loadBalancerAdapter.addRules(Arrays.asList(new TestRuleAdapter(/* isDefault */ true, 0, "default")));
        ruleInserter = new LoadBalancerRuleInserter<>(
                loadBalancerAdapter, ApplicationLoadBalancer.MAX_PRIORITY, ApplicationLoadBalancer.MAX_RULES_PER_LOADBALANCER);
        assertUniqueAscendingPriorities();
    }
    
    @Test
    public void simpleRuleInsertionTest() {
        assertEquals(1, Util.size(loadBalancerAdapter.getRules()));
        ruleInserter.addRulesAssigningUnusedPriorities(/* forceContiguous */ true, /* insertBefore */ Optional.empty(),
                Arrays.asList(new TestRuleAdapter(/* isDefault */ false, 1, "Rule 1")));
        assertEquals(2, Util.size(loadBalancerAdapter.getRules()));
        assertUniqueAscendingPriorities();
    }
    
    @Test
    public void additionalRuleInsertionTest() {
        assertEquals(1, Util.size(loadBalancerAdapter.getRules()));
        ruleInserter.addRulesAssigningUnusedPriorities(/* forceContiguous */ true, /* insertBefore */ Optional.empty(),
                Arrays.asList(new TestRuleAdapter(/* isDefault */ false, 1, "Rule 1")));
        assertEquals(2, Util.size(loadBalancerAdapter.getRules()));
        ruleInserter.addRulesAssigningUnusedPriorities(/* forceContiguous */ true, /* insertBefore */ Optional.empty(),
                Arrays.asList(new TestRuleAdapter(/* isDefault */ false, 1, "Rule 2")));
        assertUniqueAscendingPriorities();
    }
    
    // TODO add tests where "holes" in the rules base do / don't allow for a sequence of rules to be added contiguously
    
    // TODO add tests regarding inserting at another Rule's position, shifting other rules "right"
    
    // TODO assert that exceptions are thrown if there is not enough space
    
    private void assertUniqueAscendingPriorities() {
        int lastPriority = -1;
        for (final TestRuleAdapter rule : loadBalancerAdapter.getRules()) {
            assertTrue(Integer.valueOf(rule.priority()) > lastPriority);
            lastPriority = Integer.valueOf(rule.priority());
        }
    }
}
