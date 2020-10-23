package com.sap.sse.landscape.aws;

public interface HostnameCondition extends LoadBalancerRuleCondition {
    Iterable<String> getHostnames();
}
