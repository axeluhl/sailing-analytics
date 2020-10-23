package com.sap.sse.landscape.aws;

public interface SourceIPCondition extends LoadBalancerRuleCondition {
    Iterable<String> getCidrs();
}
