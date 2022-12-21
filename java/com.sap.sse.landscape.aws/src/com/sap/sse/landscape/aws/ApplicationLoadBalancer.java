package com.sap.sse.landscape.aws;

import java.util.Optional;
import java.util.regex.Pattern;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;
import com.sap.sse.landscape.aws.common.shared.RedirectDTO;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

/**
 * Represents an AWS Application Load Balancer (ALB). When created, a default configuration with the following
 * attributes should be considered: {@code access_logs.s3.enabled==true}, then setting the {@code access_logs.s3.bucket}
 * and {@code access_logs.s3.prefix}, enabling {@code deletion_protection.enabled} and setting
 * {@code idle_timeout.timeout_seconds} to the maximum value of 4000s, furthermore spanning all availability
 * zones available in the region in which the ALB is deployed and using a specific security group that
 * allows for HTTP and HTTPS traffic.<p>
 * 
 * Furthermore, two listeners are always established: the HTTP listener forwards to a dedicated target group that
 * has as its target(s) the central reverse proxy/proxies. Any HTTP request arriving there will be re-written to
 * a corresponding HTTPS request and is then expected to arrive at the HTTPS listener of the same ALB.<p>
 * 
 * The HTTPS listener contains a default route that also forwards to central reverse proxy/proxies, requiring
 * another ALB-specific target group for HTTPS traffic.<p>
 * 
 * The two default rules and the two listeners are not entirely exposed by this interface. Instead, clients
 * will only see the non-default rule set of the HTTPS listener which is used to dynamically configure the
 * landscape.
 * 
 * On https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-limits.html are some restrictions
 * listed for the load balanancer's setup. 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationLoadBalancer<ShardingKey> extends Named {
    
    static final int MAX_RULES_PER_LOADBALANCER = 100;
    static final String DNS_MAPPED_ALB_NAME_PREFIX = "DNSMapped-";
    static final Pattern ALB_NAME_PATTERN = Pattern.compile(DNS_MAPPED_ALB_NAME_PREFIX+"(.*)$");
    static final int MAX_ALBS_PER_REGION = 20;
    static final int MAX_CONDITIONS_PER_RULE = 5;
    public static final String DEFAULT_RULE_PRIORITY = "Default";
    
    /**
     * The maximum {@link Rule#priority()} that can be used within a listener
     */
    public static final int MAX_PRIORITY = 50000;
    
    /**
     * The DNS name of this load balancer; can be used, e.g., to set a CNAME DNS record pointing
     * to this load balancer.
     */
    String getDNSName();

    /**
     * Obtains a fresh copy of the rules in this load balancers HTTPS listener from the AWS API.
     */
    Iterable<Rule> getRules();
    
    void deleteRules(Rule... rulesToDelete);

    Region getRegion();

    String getArn();
    
    default String getId() {
        return getArn().substring(getArn().lastIndexOf('/')+1);
    }

    /**
     * Application load balancer rules have a {@link Rule#priority() priority} which must be unique in the scope of a
     * load balancer's listener. This way, when rules come and go, holes in the priority numbering scheme will start to
     * exist. If a set of rules is to be added ({@code rulesToAdd}), consider using
     * {@link #addRulesAssigningUnusedPriorities} to make room for interleaved or contiguous addition of the new rules.
     * 
     * @param rulesToAdd
     *            rules (without an ARN set yet), specifying which rules to add to the HTTPS listener of this load
     *            balancer. All rules must have a priority that is not used currently by the listener.
     * @return the rules created, with ARNs set
     */
    Iterable<Rule> addRules(Rule... rulesToAdd);
    
    /**
     * As the rule {@link Rule#priority() priorities} within a load balancer's listener have to be unique, this method
     * supports adding rules by assigning yet unused priorities to them. It keeps the order in which the {@code rules}
     * are passed. If {@code forceContiguous} is {@code false}, for each rule the next available priority is chosen and
     * assigned by creating a copy of the {@link Rule} object and adding it to the resulting sequence. This can lead to
     * existing rules interleaving with the rules to add while ensuring that the {@code rules} have priorities in
     * numerically ascending order, consistent with the order in which they were passed to this method.
     * <p>
     * 
     * If {@code forceContiguous} is {@code true}, the rules that result will have contiguously increasing priority
     * values, hence not interleaving with other existing rules. If there are enough contiguous unused priorities
     * available, they are selected and assigned by creating copies of the {@link Rule} objects and adding them in their
     * original order to the resulting sequence. Otherwise, the existing rules are "compressed" by re-numbering their
     * priorities to make space for the new rules at the end of the list.
     * 
     * @return copies of the original rules, with unused {@link Rule#priority() priorities} assigned, as passed already
     *         to {@link #addRules(Rule...)}.
     */
    Iterable<Rule> addRulesAssigningUnusedPriorities(boolean forceContiguous, Rule... rules);
    
    /**
     * For inserting e.g. Shard at a specific priority, we must ensure that the priority is unique in the ruleset.
     * This function shifts every priority starting at the highest priority one higher for making the priority at {@code index}
     * free. 
     * @param index
     *          index supposed to be free
     * @throws IllegalStateException
     *          gets thrown if shifting exceeds the limit of priorities
     */
    Iterable<Rule> shiftRulesToMakeSpaceAt(int index) throws IllegalStateException;
    
    /**
     * Returns the priority which should be used as the next sharding priority.
     * @param hostname
     *          hostname of replica set from which the shard gets created
     * @return priority of a rule with hostname as Host +1, if the rule it was found in is a redirect or the index of the Rule if it is a Forward.
     *          if there is no Rule with this hostname, just return the index after the last rule. This index should be used as the next shard priority.
     *          You may need to make space at this priority via {@link #shiftRulesToMakeSpaceAt(int)}
     * @throws Exception
     *          if the found priority is higher than the {@code MAX_PRIORITY}
     */         
    
    int getFirstShardingPriority(String hostname) throws IllegalStateException;
    
    Iterable<TargetGroup<ShardingKey>> getTargetGroups();

    /**
     * Deletes this application load balancer and all its {@link #getTargetGroups target groups}.
     */
    void delete() throws InterruptedException;

    void deleteListener(Listener listener);

    Listener getListener(ProtocolEnum protocol);
    
    default Rule setDefaultRedirect(String hostname, RedirectDTO redirect) {
        return setDefaultRedirect(hostname, redirect.getPath(), redirect.getQuery());
    }

    /**
     * {@link #createDefaultRedirectRule(String, String, Optional) Creates} or updates a default re-direct rule in this
     * load balancer's HTTPS listener. Such a default re-direct rule is triggered by a request for the {@code hostname}
     * with the path being {@code "/"} and sends a re-direct response to the client that replaces path and query with
     * the values specified by the {@code path} and {@code query} parameters.
     * 
     * @return the {@link Rule} that represents the default re-direct
     */
    Rule setDefaultRedirect(String hostname, String path, Optional<String> query);
    
    Rule getDefaultRedirectRule(String hostName, PlainRedirectDTO plainRedirectDTO);

    RuleCondition createHostHeaderRuleCondition(String hostname);
    
    Iterable<Rule> getRulesForTargetGroups(Iterable<TargetGroup<ShardingKey>> targetGroups);

    Iterable<Rule> replaceTargetGroupInForwardRules(TargetGroup<ShardingKey> oldTargetGroup, TargetGroup<ShardingKey> newTargetGroup );
}
