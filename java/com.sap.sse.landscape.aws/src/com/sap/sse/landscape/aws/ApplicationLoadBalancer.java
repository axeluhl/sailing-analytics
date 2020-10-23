package com.sap.sse.landscape.aws;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;

/**
 * Represents an AWS Application Load Balancer (ALB). When created, a default configuration with the following
 * attributes should be considered: {@code access_logs.s3.enabled==true}, then setting the {@code access_logs.s3.bucket}
 * and {@code access_logs.s3.prefix}, enabling {@code deletion_protection.enabled} and setting
 * {@code idle_timeout.timeout_seconds} to the maximum value of 4000s, furthermore spanning all availability
 * zones available in the region in which the ALB is deployed and using a specific security group that
 * allows for HTTP and HTTPS traffic.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationLoadBalancer extends Named {
    /**
     * The DNS name of this load balancer; can be used, e.g., to set a CNAME DNS record pointing
     * to this load balancer.
     */
    String getDNSName();

    Iterable<LoadBalancerRule> getRules();
    
    void addRules(Iterable<LoadBalancerRule> rulesToAdd);
    
    void deleteRules(Iterable<LoadBalancerRule> rulesToDelete);

    Region getRegion();

    String getArn();
}
