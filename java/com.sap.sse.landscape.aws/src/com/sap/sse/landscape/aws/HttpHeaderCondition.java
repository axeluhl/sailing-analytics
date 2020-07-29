package com.sap.sse.landscape.aws;

import java.util.Map;

public interface HttpHeaderCondition extends LoadBalancerRuleCondition {
    Map<String, String> getHeaderFieldNamesAndValues();
}
