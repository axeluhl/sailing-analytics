package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.sap.sse.common.Named;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RulePriorityPair;

/**
 * Manages a set of rules from an Application Load Balancer. This includes adding a set of rules either contiguously or
 * optionally non-contiguously to an existing set of rules that are ordered by their priorities. There are limits on the
 * maximum number of rules and on the maximum priority that can be used. Trying to add too many rules so that one of
 * these limits would have to be violated to accommodate will throw an exception.
 * <p>
 * 
 * For purposes of testability, the class is designed such that the actual {@link Rule} and {@link RulePriorityPair}
 * classes are abstracted by an interface. Default adapters are available for the actual ALB-related classes, but tests
 * may provide their own mocks instead so that it is not required to test this class against a real AWS environment.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LoadBalancerRuleInserter<ShardingKey, RA extends com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter.RuleAdapter<RA>> {
    private final LoadBalancerAdapter<RA> loadBalancerAdapter;
    private final int maxPriority;
    private final int maxRulesPerLoadBalancer;
    
    public static interface RuleAdapter<RA extends RuleAdapter<RA>> {
        boolean isDefault();
        String priority();
        String ruleArn();
        RA copyWithNewPriority(int priorityToUseForRuleCopy);
    }
    
    public static interface LoadBalancerAdapter<RA extends RuleAdapter<RA>> extends Named {
        Iterable<RA> getRules();

        void updateLoadBalancerListenerRulePriorities(List<Pair<Integer, RA>> newPrioritiesForExistingRules);

        void addRules(List<RA> result);
    }
    
    public static class ALBRuleAdapter implements RuleAdapter<ALBRuleAdapter> {
        private final Rule rule;
        
        private ALBRuleAdapter(Rule rule) {
            this.rule = rule;
        }

        @Override
        public boolean isDefault() {
            return rule.isDefault();
        }

        @Override
        public String priority() {
            return rule.priority();
        }

        @Override
        public String ruleArn() {
            return rule.ruleArn();
        }

        @Override
        public ALBRuleAdapter copyWithNewPriority(int priorityToUseForRuleCopy) {
            return new ALBRuleAdapter(rule.copy(b->b.priority(""+priorityToUseForRuleCopy).build()));
        }
        
        Rule getRule() {
            return rule;
        }
    }
    
    private static class ALBAdapter<ShardingKey> implements LoadBalancerAdapter<ALBRuleAdapter> {
        private static final long serialVersionUID = -4337640786328697695L;
        private final ApplicationLoadBalancer<ShardingKey> alb;

        private ALBAdapter(ApplicationLoadBalancer<ShardingKey> alb) {
            super();
            this.alb = alb;
        }

        @Override
        public String getName() {
            return alb.getName();
        }

        @Override
        public Iterable<ALBRuleAdapter> getRules() {
            return Util.map(alb.getRules(), r->createRuleAdapter(r));
        }

        @Override
        public void updateLoadBalancerListenerRulePriorities(
                List<Pair<Integer, ALBRuleAdapter>> newPrioritiesForExistingRules) {
            alb.getLandscape().updateLoadBalancerListenerRulePriorities(alb.getRegion(),
                    Util.map(newPrioritiesForExistingRules, p->RulePriorityPair.builder().priority(p.getA()).ruleArn(p.getB().ruleArn()).build()));
        }

        @Override
        public void addRules(List<ALBRuleAdapter> rules) {
            alb.addRules(Util.toArray(Util.map(rules, ra->ra.getRule()), new Rule[0]));
        }
        
    }
    
    public LoadBalancerRuleInserter(LoadBalancerAdapter<RA> loadBalancerAdapter, int maxPriority, int maxRulesPerLoadBalancer) {
        super();
        this.loadBalancerAdapter = loadBalancerAdapter;
        this.maxPriority = maxPriority;
        this.maxRulesPerLoadBalancer = maxRulesPerLoadBalancer;
    }
    
    public static <ShardingKey> LoadBalancerRuleInserter<ShardingKey, ALBRuleAdapter> create(
            ApplicationLoadBalancer<ShardingKey> alb, int maxPriority, int maxRulesPerLoadBalancer) {
        return new LoadBalancerRuleInserter<>(createLoadBalancerAdapter(alb), maxPriority, maxRulesPerLoadBalancer);
    }
    
    public static ALBRuleAdapter createRuleAdapter(Rule rule) {
        return new ALBRuleAdapter(rule);
    }
    
    static <ShardingKey> ALBAdapter<ShardingKey> createLoadBalancerAdapter(ApplicationLoadBalancer<ShardingKey> alb) {
        return new ALBAdapter<ShardingKey>(alb);
    }

    public List<RA> addRulesAssigningUnusedPriorities(boolean forceContiguous, Optional<RA> insertBefore,
            Iterable<RA> rules) {
        final Iterable<RA> existingRules = loadBalancerAdapter.getRules();
        final int rulesSize = Util.size(rules);
        if (Util.size(existingRules)-1 + rulesSize > maxPriority) { // -1 due to the default rule being part of existingRules
            throw new IllegalArgumentException("The "+rulesSize+" new rules won't find enough unused priority numbers because there are already "+
                    (Util.size(existingRules)-1)+" of them and together they would exceed the maximum of "+maxPriority+" by "+
                    (Util.size(existingRules)-1 + rulesSize - maxPriority));
        }
        if (Util.size(existingRules) + rulesSize > maxRulesPerLoadBalancer) {
            throw new IllegalArgumentException("The " + rulesSize + " new rules would make the ALB " + loadBalancerAdapter.getName()
                    + " exceed its maximum number of rules (" + maxPriority + ") by "
                    + (Util.size(existingRules) + rulesSize - maxRulesPerLoadBalancer));
        }
        final List<RA> result = new ArrayList<>(rulesSize);
        final List<RA> sortedExistingNonDefaultRules = new ArrayList<>(Util.size(existingRules)-1);
        Util.addAll(Util.filter(existingRules, r->!r.isDefault()), sortedExistingNonDefaultRules);
        Collections.sort(sortedExistingNonDefaultRules, (r1, r2)->Integer.valueOf(r1.priority()).compareTo(Integer.valueOf(r2.priority())));
        final int stepwidth;
        if (forceContiguous) {
            stepwidth = rulesSize;
        } else {
            stepwidth = 1;
        }
        final Iterator<RA> rulesIterator = rules.iterator();
        int previousPriority = 0;
        final Iterator<RA> existingRulesIter = sortedExistingNonDefaultRules.iterator();
        while (rulesIterator.hasNext()) {
            // find next available slot
            int nextPriority = maxPriority+1; // if no further rule exists, the usable gap ends after MAX_PRIORITY
            final RuleAdapter<?>[] nextRule = new RuleAdapter<?>[1]; // using an array to make it final so we can access it inside the mapping function below
            boolean stillLookingForRuleToInsertBefore = insertBefore.isPresent();
            while (existingRulesIter.hasNext() &&
                    ((nextPriority=Integer.valueOf((nextRule[0]=existingRulesIter.next()).priority())) <= previousPriority+stepwidth) ||
                     (stillLookingForRuleToInsertBefore && (stillLookingForRuleToInsertBefore=insertBefore.map(rule->!rule.ruleArn().equals(nextRule[0].ruleArn())).orElse(false)))) {
                // not enough space for stepwidth many rules; keep on searching
                previousPriority = nextPriority;
                if (!existingRulesIter.hasNext()) {
                    nextPriority = maxPriority+1;
                }
            }
            if (previousPriority+stepwidth > maxPriority) {
                if (forceContiguous) {
                    previousPriority = squeezeExistingRulesAndReturnLastUsedPriority(sortedExistingNonDefaultRules);
                    nextPriority = maxPriority+1;
                    // we previously checked already that there is enough room for the new set of rules
                    assert previousPriority + rulesSize <= maxPriority;
                } else {
                    throw new IllegalStateException(
                            "The " + rulesSize + " new rules don't fit into the existing rule set of load balancer "
                                    + loadBalancerAdapter.getName() + " without exceeding the maximum priority of " + maxPriority);
                }
            }
            while (rulesIterator.hasNext() && ++previousPriority < nextPriority) {
                final int priorityToUseForNextRule = previousPriority;
                result.add(rulesIterator.next().copyWithNewPriority(priorityToUseForNextRule));
            }
        }
        loadBalancerAdapter.addRules(result);
        return result;
    }
    
    private int squeezeExistingRulesAndReturnLastUsedPriority(final List<RA> sortedExistingNonDefaultRules) {
        final List<Pair<Integer, RA>> newPrioritiesForExistingRules = new LinkedList<>();
        int priority = 0;
        for (final RA existingRule : sortedExistingNonDefaultRules) {
            newPrioritiesForExistingRules.add(new Pair<>(++priority, existingRule));
        }
        loadBalancerAdapter.updateLoadBalancerListenerRulePriorities(newPrioritiesForExistingRules);
        return priority;
    }
}
