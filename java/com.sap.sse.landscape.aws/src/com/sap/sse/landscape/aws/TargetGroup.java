package com.sap.sse.landscape.aws;

import java.util.Collections;
import java.util.Map;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TagDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

/**
 * Represents a target group that can be configured as the forwarding target of a {@link LoadBalancerRule}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TargetGroup<ShardingKey> extends Named {
    
    final public static int MAX_TARGETGROUP_NAME_LENGTH = 32;
    final public static String SAILING_TARGET_GROUP_NAME_PREFIX = "S-";
    final public static String MASTER_SUFFIX = "-m";
    final public static String TEMP_SUFFIX = "-TMP";
    
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
     * 
     * @return
     *          returns all tag descriptions for this target group
     */
    Iterable<TagDescription> getTagDescriptions();
    
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

    /**
     * Obtains the load balancer based on the {@link #getLoadBalancerArn() load balancer ARN}. Note that this
     * will not lead to a dynamic discovery of this target group's load balancer; if this object was created
     * without explicitly assigning a load balancer ARN and at that time the target group was not the target of
     * any load balancer's rule, no load balancer ARN will be set, and hence no load balancer will be returned
     * by this method.
     */
    ApplicationLoadBalancer<ShardingKey> getLoadBalancer();

    String getLoadBalancerArn();
}
