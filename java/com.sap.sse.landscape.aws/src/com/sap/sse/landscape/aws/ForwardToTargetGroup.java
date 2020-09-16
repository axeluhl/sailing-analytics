package com.sap.sse.landscape.aws;

public interface ForwardToTargetGroup extends LoadBalancerRuleAction {
    TargetGroup getTargetGroupToForwardTo();
}
