package com.sap.sse.landscape.aws;

/**
 * One or more conditions may be set; within each condition a conjunction ("OR") for more than
 * one pattern or value may be possible, however limited in size (up to four or so).
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface LoadBalancerRule {
    HostnameCondition getHostnameCondition();
    HttpHeaderCondition getHttpHeaderCondition();
    MethodCondition getMethodCondition();
    PathCondition getPathCondition();
    QueryStringCondition getQueryStringCondition();
    SourceIPCondition getSourceIPCondition();
    LoadBalancerRuleAction getAction();
}
