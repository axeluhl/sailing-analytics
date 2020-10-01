package com.sap.sse.landscape.aws;

import java.util.Collections;
import java.util.Map;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AwsRegion;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

/**
 * Represents a target group that can be configured as the forwarding target of a {@link LoadBalancerRule}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TargetGroup<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Named {
    AwsRegion getRegion();
    
    Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> getRegisteredTargets();
    
    default void addTarget(AwsInstance<ShardingKey, MetricsT> target) {
        addTargets(Collections.singleton(target));
    }
    
    void addTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets);
    
    default void removeTarget(AwsInstance<ShardingKey, MetricsT> target) {
        removeTargets(Collections.singleton(target));
    }
    
    void removeTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets);

    String getTargetGroupArn();
}
