package com.sap.sse.landscape.aws;

import java.util.Collections;

import com.sap.sse.common.Named;

/**
 * Represents a target group that can be configured as the forwarding target of a {@link LoadBalancerRule}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TargetGroup extends Named {
    Iterable<AwsInstance> getRegisteredTargets();
    
    default void addTarget(AwsInstance target) {
        addTargets(Collections.singleton(target));
    }
    
    void addTargets(Iterable<AwsInstance> targets);
    
    default void removeTarget(AwsInstance target) {
        removeTargets(Collections.singleton(target));
    }
    
    void removeTargets(Iterable<AwsInstance> targets);
}
