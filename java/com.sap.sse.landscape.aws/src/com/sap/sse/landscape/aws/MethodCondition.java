package com.sap.sse.landscape.aws;

public interface MethodCondition extends LoadBalancerRuleCondition {
    Iterable<String> getMethods();
}
