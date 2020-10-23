package com.sap.sse.landscape.aws;

public interface ReturnFixedResponse extends LoadBalancerRuleAction {
    int getResponseCode();
    String getContentType();
    String getResponseBody();
}
