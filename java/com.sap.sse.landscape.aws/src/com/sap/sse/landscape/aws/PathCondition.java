package com.sap.sse.landscape.aws;

public interface PathCondition extends LoadBalancerRuleCondition {
    Iterable<String> getPathPatterns();
}
