package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig.Builder;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionStatusCodeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RulePriorityPair;

public class ApplicationLoadBalancerImpl<ShardingKey>
implements ApplicationLoadBalancer<ShardingKey> {
    private static final long serialVersionUID = -5297220031399131769L;
    
    /**
     * The maximum {@link Rule#priority()} that can be used within a listener
     */
    private static final int MAX_PRIORITY = 50000;
    
    private final LoadBalancer loadBalancer;

    private final Region region;

    private final AwsLandscape<ShardingKey> landscape;
    
    public ApplicationLoadBalancerImpl(Region region, LoadBalancer loadBalancer, AwsLandscape<ShardingKey> landscape) {
        this.region = region;
        this.loadBalancer = loadBalancer;
        this.landscape = landscape;
    }

    @Override
    public String getName() {
        return loadBalancer.loadBalancerName();
    }

    @Override
    public String getDNSName() {
        return loadBalancer.dnsName();
    }

    @Override
    public String getArn() {
        return loadBalancer.loadBalancerArn();
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public Iterable<Rule> getRules() {
        final Listener httpsListener = getListener(ProtocolEnum.HTTPS);
        return landscape.getLoadBalancerListenerRules(httpsListener, getRegion());
    }
    
    @Override
    public void deleteListener(Listener listener) {
        landscape.deleteLoadBalancerListener(getRegion(), listener);
    }

    @Override
    public Listener getListener(ProtocolEnum protocol) {
        return Util.filter(landscape.getListeners(this), l->l.protocol() == protocol).iterator().next();
    }

    @Override
    public Iterable<Rule> addRules(Rule... rulesToAdd) {
        return landscape.createLoadBalancerListenerRules(region, getListener(ProtocolEnum.HTTPS), rulesToAdd);
    }

    @Override
    public Iterable<Rule> addRulesAssigningUnusedPriorities(boolean forceContiguous, Rule... rules) {
        final Iterable<Rule> existingRules = getRules();
        if (Util.size(existingRules)-1 + rules.length > MAX_PRIORITY) { // -1 due to the default rule being part of existingRules
            throw new IllegalArgumentException("The "+rules.length+" new rules won't find enough unused priority numbers because there are already "+
                    (Util.size(existingRules)-1)+" of them and together they would exceed the maximum of "+MAX_PRIORITY+" by "+
                    (Util.size(existingRules)-1 + rules.length - MAX_PRIORITY));
        }
        final List<Rule> result = new ArrayList<>(rules.length);
        final List<Rule> sortedExistingNonDefaultRules = new ArrayList<>(Util.size(existingRules)-1);
        Util.addAll(Util.filter(existingRules, r->!r.isDefault()), sortedExistingNonDefaultRules);
        Collections.sort(sortedExistingNonDefaultRules, (r1, r2)->Integer.valueOf(r1.priority()).compareTo(Integer.valueOf(r2.priority())));
        final int stepwidth;
        if (forceContiguous) {
            stepwidth = rules.length;
        } else {
            stepwidth = 1;
        }
        int rulesIndex = 0;
        int previousPriority = 0;
        final Iterator<Rule> existingRulesIter = sortedExistingNonDefaultRules.iterator();
        while (rulesIndex < rules.length) {
            // find next available slot
            int nextPriority = MAX_PRIORITY+1; // if no further rule exists, the usable gap ends after MAX_PRIORITY
            while (existingRulesIter.hasNext() && (nextPriority=Integer.valueOf(existingRulesIter.next().priority())) <= previousPriority+stepwidth) {
                // not enough space for stepwidth many rules; keep on searching
                previousPriority = nextPriority;
                if (!existingRulesIter.hasNext()) {
                    nextPriority = MAX_PRIORITY+1;
                }
            }
            if (previousPriority+stepwidth > MAX_PRIORITY) {
                if (forceContiguous) {
                    previousPriority = squeezeExistingRulesAndReturnLastUsedPriority(sortedExistingNonDefaultRules);
                    nextPriority = MAX_PRIORITY+1;
                    // we previously checked already that there is enough room for the new set of rules
                    assert previousPriority + rules.length <= MAX_PRIORITY;
                } else {
                    throw new IllegalStateException(
                            "The " + rules.length + " new rules don't fit into the existing rule set of load balancer "
                                    + getName() + " without exceeding the maximum priority of " + MAX_PRIORITY);
                }
            }
            while (rulesIndex < rules.length && ++previousPriority < nextPriority) {
                final int priorityToUseForNextRule = previousPriority;
                result.add(rules[rulesIndex++].copy(b->b.priority(""+priorityToUseForNextRule)));
            }
        }
        addRules(result.toArray(new Rule[0]));
        return result;
    }

    private int squeezeExistingRulesAndReturnLastUsedPriority(final List<Rule> sortedExistingNonDefaultRules) {
        final List<RulePriorityPair> newPrioritiesForExistingRules = new LinkedList<>();
        int priority = 0;
        for (final Rule existingRule : sortedExistingNonDefaultRules) {
            newPrioritiesForExistingRules.add(RulePriorityPair.builder().ruleArn(existingRule.ruleArn()).priority(++priority).build());
        }
        landscape.updateLoadBalancerListenerRulePriorities(getRegion(), newPrioritiesForExistingRules);
        return priority;
    }

    @Override
    public void deleteRules(Rule... rulesToDelete) {
        landscape.deleteLoadBalancerListenerRules(region, rulesToDelete);
    }

    @Override
    public Iterable<TargetGroup<ShardingKey>> getTargetGroups() {
        return landscape.getTargetGroupsByLoadBalancerArn(getRegion(), getArn());
    }
    
    private void deleteAllRules() {
        for (final Rule rule : getRules()) {
            if (!rule.isDefault()) {
                deleteRules(rule);
            }
        }
    }

    @Override
    public void delete() throws InterruptedException {
        // first obtain the target groups to which, based on the current ALB configuration, traffic is forwarded
        final Set<TargetGroup<ShardingKey>> targetGroups = new HashSet<>();
        Util.addAll(getTargetGroups(), targetGroups); // do this before deleting the ALB because then its ARN isn't known anymore
        // now delete the rules to free up all target groups to which the ALB could have forwarded, except the default rule
        deleteAllRules();
        deleteAllListeners();
        landscape.deleteLoadBalancer(this);
        Thread.sleep(Duration.ONE_SECOND.times(5).asMillis()); // wait a bit until the target groups are no longer considered "in use"
        // now that all target groups the ALB used are freed up, delete them:
        for (final TargetGroup<?> targetGroup : targetGroups) {
            landscape.deleteTargetGroup(targetGroup);
        }
    }
    
    private void deleteListener(ProtocolEnum protocol) {
        final Listener httpListener = getListener(protocol);
        if (httpListener != null) {
            deleteListener(httpListener);
        }
    }
    
    private void deleteAllListeners() {
        deleteListener(ProtocolEnum.HTTP);
        deleteListener(ProtocolEnum.HTTPS);
    }

    @Override
    public Rule setDefaultRedirect(String hostname, String pathWithLeadingSlash, Optional<String> query) {
        return Util.stream(getRules()).filter(r->isDefaultRedirectRule(r, hostname)).findAny()
            .map(defaultRedirectRule->updateDefaultRedirectRule(defaultRedirectRule.ruleArn(), hostname, pathWithLeadingSlash, query))
            .orElseGet(()->{
                final Rule defaultRedirectRule = createDefaultRedirectRule(hostname, pathWithLeadingSlash, query);
                addRulesAssigningUnusedPriorities(/* forceContiguous */ false, defaultRedirectRule);
                return defaultRedirectRule;
            });
    }
    
    @Override
    public Rule getDefaultRedirectRule(String hostName, PlainRedirectDTO redirect) {
        final Rule defaultRedirectRule = createDefaultRedirectRule(hostName, redirect.getPath(), redirect.getQuery());
        return defaultRedirectRule;
    }

    /**
     * Creates a new rule in the HTTPS listener of this load balancer. The rule fires when {@code "/"} is
     * the path ("empty" path) and the hostname header matches the value provided by the {@code hostname}
     * parameter. It sends a redirecting response with status code 302, redirecting to the same host, same
     * protocol and port and the path specified by the {@code pathWithLeadingSlash} parameter.<p>
     */
    private Rule createDefaultRedirectRule(String hostname, String pathWithLeadingSlash, Optional<String> query) {
        return getDefaultRedirectRuleBuilder(hostname, pathWithLeadingSlash, query).build();
    }

    private software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule.Builder getDefaultRedirectRuleBuilder(
            String hostname, String pathWithLeadingSlash, Optional<String> query) {
        return Rule.builder()
                .conditions(RuleCondition.builder().field("path-pattern").pathPatternConfig(ppc->ppc.values("/")).build(),
                            createHostHeaderRuleCondition(hostname))
                .actions(createDefaultRedirectAction(pathWithLeadingSlash, query));
    }

    private Action createDefaultRedirectAction(String pathWithLeadingSlash, Optional<String> query) {
        Builder redirectConfigBuilder = RedirectActionConfig.builder()
                .protocol("#{protocol}")
                .port("#{port}")
                .host("#{host}")
                .path(pathWithLeadingSlash)
                .statusCode(RedirectActionStatusCodeEnum.HTTP_302);
        query.ifPresent(q->redirectConfigBuilder.query(q));
        return Action.builder().type(ActionTypeEnum.REDIRECT).redirectConfig(redirectConfigBuilder.build()).build();
    }
    
    @Override
    public RuleCondition createHostHeaderRuleCondition(String hostname) {
        return RuleCondition.builder().field("host-header").hostHeaderConfig(hhcb->hhcb.values(hostname)).build();
    }

    private Rule updateDefaultRedirectRule(String defaultRedirectRuleArn, String hostname, String pathWithLeadingSlash, Optional<String> query) {
        final Rule updatedRule = getDefaultRedirectRuleBuilder(hostname, pathWithLeadingSlash, query)
                .ruleArn(defaultRedirectRuleArn)
                .build();
        landscape.updateLoadBalancerListenerRule(getRegion(), updatedRule);
        return updatedRule;
    }

    /**
     * A {@code rule} is considered to be the default redirect rule for {@code hostname} if it has a {@code host-header}
     * condition for that {@code hostname} and a {@code path-pattern} condition for exactly one path, {@code "/"}.
     */
    private boolean isDefaultRedirectRule(Rule rule, String hostname) {
        return
                rule.conditions().stream().filter(condition->condition.field().equals("host-header")
                                               && condition.hostHeaderConfig().values().contains(hostname)).findAny().isPresent()
                &&
                rule.conditions().stream().filter(condition->condition.field().equals("path-pattern")
                                               && condition.pathPatternConfig().values().size() == 1
                                               && condition.pathPatternConfig().values().contains("/")).findAny().isPresent();
    }
}
