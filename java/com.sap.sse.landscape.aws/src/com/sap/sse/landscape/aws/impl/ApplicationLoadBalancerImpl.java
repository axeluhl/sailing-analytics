package com.sap.sse.landscape.aws.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;
import com.sap.sse.landscape.aws.impl.LoadBalancerRuleInserter.ALBRuleAdapter;

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
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

public class ApplicationLoadBalancerImpl<ShardingKey>
implements ApplicationLoadBalancer<ShardingKey> {
    private static final Logger logger = Logger.getLogger(ApplicationLoadBalancerImpl.class.getName());
    
    private static final long serialVersionUID = -5297220031399131769L;
    
    private final LoadBalancer loadBalancer;

    private final Region region;

    private final AwsLandscape<ShardingKey> landscape;
    
    public ApplicationLoadBalancerImpl(Region region, LoadBalancer loadBalancer, AwsLandscape<ShardingKey> landscape) {
        this.region = region;
        this.loadBalancer = loadBalancer;
        this.landscape = landscape;
    }
    
    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return landscape;
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
    public String getVpcId() {
        return loadBalancer.vpcId();
    }
    
    @Override
    public List<String> getSecurityGroupIds() {
        return loadBalancer.securityGroups();
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
    public int getFirstShardingPriority(String hostname) throws IllegalStateException {
        final Iterable<Rule> rules = getRules();
        final TreeMap<Integer, Rule> rulesSorted = getRulesSorted(rules);
        final Iterator<Entry<Integer, Rule>> iterSorted = rulesSorted.entrySet().iterator();
        int priorityToReturn = -1;
        outher : while (iterSorted.hasNext()) {
            final Rule r = iterSorted.next().getValue();
            for (RuleCondition con : r.conditions()) {
                if (con.hostHeaderConfig() != null && con.hostHeaderConfig().values().contains(hostname)) {
                    for (Action a : r.actions()) {
                        if (a.forwardConfig() != null) {
                            priorityToReturn = Integer.parseInt(r.priority());
                            break outher;
                        } else if (a.redirectConfig() != null) {
                            priorityToReturn = Integer.parseInt(r.priority()) + 1;
                            break outher;
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (!iterSorted.hasNext()) {
                priorityToReturn = Integer.parseInt(r.priority()) + 1;
                break outher;
            }
        }
        if (priorityToReturn > MAX_PRIORITY) {
            throw new IllegalStateException("Index higher than " + MAX_PRIORITY + "!");
        }
        return priorityToReturn;
    }

    private TreeMap<Integer, Rule> getRulesSorted(Iterable<Rule> rules) {
        final Iterator<Rule> iter = rules.iterator();
        final TreeMap<Integer, Rule> rulesSorted = new TreeMap<Integer, Rule>();
        // create Map with every priority
        while (iter.hasNext()) {
            final Rule r = iter.next();
            try {
                if (!r.priority().equalsIgnoreCase(ApplicationLoadBalancer.DEFAULT_RULE_PRIORITY)) {
                    rulesSorted.put(Integer.valueOf(r.priority()), r);
                }
            } catch (Exception e) {
                // Case where prio is not a number, e.g. 'Default' gets ignored.
                logger.log(Level.WARNING, "Priority '" + r.priority() + "' couldn't be parsed as an Integer.");
            }
        }
        return rulesSorted;
    }

    @Override
    public Iterable<Rule> addRulesAssigningUnusedPriorities(boolean forceContiguous, Optional<Rule> insertBefore, Rule... rules) {
        final List<Rule> rulesAsList = Arrays.asList(rules);
        return Util.map(LoadBalancerRuleInserter.create(this, MAX_PRIORITY, MAX_RULES_PER_LOADBALANCER).addRulesAssigningUnusedPriorities(
                forceContiguous, insertBefore.map(LoadBalancerRuleInserter::createRuleAdapter),
                Util.map(rulesAsList, LoadBalancerRuleInserter::createRuleAdapter)), ALBRuleAdapter::getRule);
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
                return insertAndReturnDefaultRedirectRule(hostname, pathWithLeadingSlash, query);
            });
    }

    /**
     * Checks the load balancer rule set size. If already at its maximum, an {@link IllegalStateException} is thrown because
     * adding a default rule is not possible, and this method does not handle moving an appplication replica set to a different
     * load balancer. If there is space for at least one more rule, searches this load balancer's rule set for the first rule
     * for hostname header {@code hostname}. It then uses {@link #shiftRulesToMakeSpaceAt(int)} for that position to make
     * space for the new default redirect rule.
     * 
     * @return the new default redirect rule that was inserted into this load balancer's HTTPS listener's rule set
     */
    private Rule insertAndReturnDefaultRedirectRule(String hostname, String pathWithLeadingSlash, Optional<String> query) {
        final Rule defaultRedirectRule = createDefaultRedirectRule(hostname, pathWithLeadingSlash, query);
        addRulesAssigningUnusedPriorities(/* forceContiguous */ false,
                Util.stream(getRules()).filter(rule->rule.conditions().stream().anyMatch(condition->condition.field().equals("host-header") && condition.hostHeaderConfig().values().contains(hostname))).findFirst(),
                defaultRedirectRule);
        return defaultRedirectRule;
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
    
    @Override
    public Iterable<Rule> getRulesForTargetGroups(Iterable<TargetGroup<ShardingKey>> targetGroups) {
        ArrayList<Rule> ret = new ArrayList<Rule>();
        for (Rule rule : getRules()) {
            for (Action action : rule.actions()) {
                if (action.type() == ActionTypeEnum.FORWARD) {
                    for (String arn : Util.map(action.forwardConfig().targetGroups(), s -> s.targetGroupArn())) {
                        for (String targetArn : Util.map(targetGroups, s -> s.getTargetGroupArn())) {
                            if (arn.equals(targetArn)) {
                                ret.add(rule);
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * Goes through all rules in this load balancer and replaces every target group in a forward-action that has the
     * same ARN as the {@code oldTargetgroup} with the {@code newTargetgroup}
     */
    @Override
    public Iterable<Rule> replaceTargetGroupInForwardRules(TargetGroup<ShardingKey> oldTargetGroup,
            TargetGroup<ShardingKey> newTargetGroup) {
        Iterable<Rule> rules = getRules();
        Collection<Rule> modifiedRules = new ArrayList<>();
        for (Rule r : rules) {
            int i = 0;
            boolean modified = false;
            Action[] newActions = new Action[r.actions().size()];
            for (Action a : r.actions()) {
                if (a.forwardConfig() != null && a.targetGroupArn().equals(oldTargetGroup.getTargetGroupArn())) {
                    newActions[i++] = createForwardToTargetGroupAction(newTargetGroup);
                    modified = true;
                } else {
                    newActions[i++] = a;
                }
            }
            if (modified) {
                landscape.modifyRuleActions(region, Rule.builder().actions(newActions).ruleArn(r.ruleArn()).build())
                    .forEach(t -> modifiedRules.add(t));
            }
        }
        return modifiedRules;
    }
    
    private Action createForwardToTargetGroupAction(TargetGroup<ShardingKey> targetGroup) {
        return Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc -> fc
                .targetGroups(TargetGroupTuple.builder().targetGroupArn(targetGroup.getTargetGroupArn()).build()))
                .build();
    }
}