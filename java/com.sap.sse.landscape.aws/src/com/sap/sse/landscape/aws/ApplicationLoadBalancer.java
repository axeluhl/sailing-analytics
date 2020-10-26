package com.sap.sse.landscape.aws;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

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
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationLoadBalancer<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Named {
    /**
     * The DNS name of this load balancer; can be used, e.g., to set a CNAME DNS record pointing
     * to this load balancer.
     */
    String getDNSName();

    Iterable<Rule> getRules();
    
    void deleteRules(Rule... rulesToDelete);

    Region getRegion();

    String getArn();

    /**
     * @param rulesToAdd
     *            rules (without an ARN set yet), specifying which rules to add to the HTTPS listener of this load
     *            balancer
     * @return the rules created, with ARNs set
     */
    Iterable<Rule> addRules(Rule... rulesToAdd);
    
    Iterable<TargetGroup<ShardingKey, MetricsT>> getTargetGroups();

    /**
     * Deletes this application load balancer and all its {@link #getTargetGroups target groups}.
     */
    void delete() throws InterruptedException;

    void deleteListener(Listener listener);
}
