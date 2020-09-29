package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public interface ForwardToTargetGroup<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends LoadBalancerRuleAction {
    TargetGroup<ShardingKey, MetricsT> getTargetGroupToForwardTo();
}
