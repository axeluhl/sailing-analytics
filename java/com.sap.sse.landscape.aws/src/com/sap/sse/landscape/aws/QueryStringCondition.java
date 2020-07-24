package com.sap.sse.landscape.aws;

import java.util.Map;

public interface QueryStringCondition extends LoadBalancerRuleCondition {
    Map<String, String> getParameterNamesAndPatterns();
}
