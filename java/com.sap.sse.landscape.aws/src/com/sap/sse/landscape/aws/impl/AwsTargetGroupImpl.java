package com.sap.sse.landscape.aws.impl;

import java.util.Map;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

public class AwsTargetGroupImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends NamedImpl implements TargetGroup<ShardingKey, MetricsT> {
    private static final long serialVersionUID = -5442598262397393201L;
    private final String arn;
    private final AwsLandscape<ShardingKey, MetricsT> landscape;
    private final AwsRegion region;

    public AwsTargetGroupImpl(AwsLandscape<ShardingKey, MetricsT> landscape, AwsRegion region, String targetGroupName, String arn) {
        super(targetGroupName);
        this.arn = arn;
        this.landscape = landscape;
        this.region = region;
    }

    public AwsRegion getRegion() {
        return region;
    }

    @Override
    public Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> getRegisteredTargets() {
        return landscape.getTargetHealthDescriptions(this);
    }

    @Override
    public void addTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        // TODO Implement AwsTargetGroupImpl.addTargets(...)
    }

    @Override
    public void removeTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        // TODO Implement AwsTargetGroupImpl.removeTargets(...)
    }

    @Override
    public String getTargetGroupArn() {
        return arn;
    }
}
