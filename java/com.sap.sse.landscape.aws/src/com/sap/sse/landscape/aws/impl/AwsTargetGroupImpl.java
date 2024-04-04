package com.sap.sse.landscape.aws.impl;

import java.util.Map;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TagDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;

public class AwsTargetGroupImpl<ShardingKey>
extends NamedImpl implements TargetGroup<ShardingKey> {
    private static final long serialVersionUID = -5442598262397393201L;
    private final String arn;
    private final String loadBalancerArn;
    private final AwsLandscape<ShardingKey> landscape;
    private final Region region;
    private final ProtocolEnum protocol;
    private final Integer port;
    private final ProtocolEnum healthCheckProtocol;
    private final Integer healthCheckPort;
    private final String healthCheckPath;

    public AwsTargetGroupImpl(AwsLandscape<ShardingKey> landscape, Region region, String targetGroupName, String arn,
            String loadBalancerArn, ProtocolEnum protocol, Integer port, ProtocolEnum healthCheckProtocol,
            Integer healthCheckPort, String healthCheckPath) {
        super(targetGroupName);
        this.arn = arn;
        this.loadBalancerArn = loadBalancerArn;
        this.landscape = landscape;
        this.region = region;
        this.protocol = protocol;
        this.port = port;
        this.healthCheckProtocol = healthCheckProtocol;
        this.healthCheckPort = healthCheckPort;
        this.healthCheckPath = healthCheckPath;
    }
    
    private software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup getAwsTargetGroup() {
        return landscape.getAwsTargetGroupByArn(getRegion(), getTargetGroupArn());
    }
    
    public Iterable<TagDescription> getTagDescriptions() {
        return landscape.getTargetGroupTags(arn, region);
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public Map<AwsInstance<ShardingKey>, TargetHealth> getRegisteredTargets() {
        return landscape.getTargetHealthDescriptions(this);
    }
    
    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancer() {
        final software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup targetGroup = getAwsTargetGroup();
        final ApplicationLoadBalancer<ShardingKey> result;
        if (targetGroup.hasLoadBalancerArns() && !targetGroup.loadBalancerArns().isEmpty()) {
            result = new ApplicationLoadBalancerImpl<>(getRegion(), landscape.getAwsLoadBalancer(
                targetGroup.loadBalancerArns().iterator().next(), getRegion()), landscape);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void addTargets(Iterable<AwsInstance<ShardingKey>> targets) {
        if (!Util.isEmpty(targets)) {
            landscape.addTargetsToTargetGroup(this, targets);
        }
    }

    @Override
    public void removeTargets(Iterable<AwsInstance<ShardingKey>> targets) {
        if (!Util.isEmpty(targets)) {
            landscape.removeTargetsFromTargetGroup(this, targets);
        }
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public ProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Integer getHealthCheckPort() {
        return healthCheckPort;
    }

    @Override
    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    @Override
    public ProtocolEnum getHealthCheckProtocol() {
        return healthCheckProtocol;
    }
    
    @Override
    public String getLoadBalancerArn() {
        return loadBalancerArn;
    }

    @Override
    public String getTargetGroupArn() {
        return arn;
    }

    @Override
    public void setHealthCheckPath(String healthCheckPath) {
        landscape.setTargetGroupHealthcheckPath(this, healthCheckPath);
    }
}
