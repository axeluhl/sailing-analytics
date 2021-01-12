package com.sap.sse.landscape.aws.impl;

import java.util.Map;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

public class AwsTargetGroupImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends NamedImpl implements TargetGroup<ShardingKey, MetricsT> {
    private static final long serialVersionUID = -5442598262397393201L;
    private final String arn;
    private final AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape;
    private final Region region;

    public AwsTargetGroupImpl(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape, Region region, String targetGroupName, String arn) {
        super(targetGroupName);
        this.arn = arn;
        this.landscape = landscape;
        this.region = region;
    }
    
    private software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroup() {
        return landscape.getAwsTargetGroupByArn(getRegion(), getTargetGroupArn());
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public Map<AwsInstance<ShardingKey, MetricsT>, TargetHealth> getRegisteredTargets() {
        return landscape.getTargetHealthDescriptions(this);
    }
    
    @Override
    public ApplicationLoadBalancer<ShardingKey, MetricsT> getLoadBalancer() {
        final software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup targetGroup = getAwsTargetGroup();
        final ApplicationLoadBalancer<ShardingKey, MetricsT> result;
        if (targetGroup.hasLoadBalancerArns() && !targetGroup.loadBalancerArns().isEmpty()) {
            result = new ApplicationLoadBalancerImpl<>(getRegion(), landscape.getAwsLoadBalancer(
                targetGroup.loadBalancerArns().iterator().next(), getRegion()), landscape);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void addTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        landscape.addTargetsToTargetGroup(this, targets);
    }

    @Override
    public void removeTargets(Iterable<AwsInstance<ShardingKey, MetricsT>> targets) {
        landscape.removeTargetsFromTargetGroup(this, targets);
    }

    @Override
    public String getTargetGroupArn() {
        return arn;
    }
}
