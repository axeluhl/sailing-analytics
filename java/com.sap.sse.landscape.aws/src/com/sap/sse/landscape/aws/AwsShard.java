package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.application.Shard;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;


/**
 * A shard is represented by a target group that has its own auto-scaling group and which handles a number of sharding
 * keys.
 * <p>
 * 
 * Technically, the shard's name is represented as a tag value on the {@link #getTargetGroup() target group}, hence the
 * restrictions for AWS tag values apply. The tag's key is {@link ShardName.TAG_KEY}. 
 *
 * <p>
 *
 * Routing to shards happens at the {@link #getTargetGroup() target group's} {@link TargetGroup#getLoadBalancer() load
 * balancer}, or more precisely, through the load balancer's HTTPS listener's rule set. Each shard requires a number of rule pairs
 * of which one rule forwards requests based on an HTTP header field requiring the request to be handled by a replica,
 * and the other one based on the request using the GET request method. Another rule pair is required per three sharding keys
 * to be handled, due to the restriction on the number of conditions a rule may have.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <ShardingKey>
 */
public interface AwsShard<ShardingKey> extends Shard<ShardingKey> {
    TargetGroup<ShardingKey> getTargetGroup();
    
    AwsAutoScalingGroup getAutoScalingGroup();
    
    ApplicationLoadBalancer<ShardingKey> getLoadBalancer();
    
    String getReplicaSetName();
    
    Iterable<Rule> getRules();
}
