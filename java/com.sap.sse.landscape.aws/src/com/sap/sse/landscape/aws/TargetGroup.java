package com.sap.sse.landscape.aws;

import java.util.Collections;
import java.util.Map;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

/**
 * Represents a target group that can be configured as the forwarding target of a {@link LoadBalancerRule}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TargetGroup<ShardingKey> extends Named {
    Region getRegion();
    
    Map<AwsInstance<ShardingKey>, TargetHealth> getRegisteredTargets();
    
    default void addTarget(AwsInstance<ShardingKey> target) {
        addTargets(Collections.singleton(target));
    }
    
    void addTargets(Iterable<AwsInstance<ShardingKey>> targets);
    
    default void removeTarget(AwsInstance<ShardingKey> target) {
        removeTargets(Collections.singleton(target));
    }
    
    void removeTargets(Iterable<AwsInstance<ShardingKey>> targets);
    
    /**
     * @return the traffic port
     */
    Integer getPort();
    
    /**
     * @return the traffic protocol; usually either one of {@link ProtocolEnum#HTTP} or {@link ProtocolEnum#HTTPS}
     */
    ProtocolEnum getProtocol();
    
    Integer getHealthCheckPort();
    
    String getHealthCheckPath();
    
    ProtocolEnum getHealthCheckProtocol();
    
    String getTargetGroupArn();

    default String getId() {
        return getTargetGroupArn().substring(getTargetGroupArn().lastIndexOf('/')+1);
    }

    ApplicationLoadBalancer<ShardingKey> getLoadBalancer();

    String getLoadBalancerArn();
}
