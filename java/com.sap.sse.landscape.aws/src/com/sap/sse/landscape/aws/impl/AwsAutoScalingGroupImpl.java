package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;

public class AwsAutoScalingGroupImpl implements AwsAutoScalingGroup {
    private static final long serialVersionUID = -6244055510377604441L;
    private final AutoScalingGroup autoScalingGroup;
    private final LaunchTemplate launchTemplate;
    private final Region region;

    public AwsAutoScalingGroupImpl(AutoScalingGroup autoScalingGroup, LaunchTemplate launchTemplate, Region region) {
        super();
        this.autoScalingGroup = autoScalingGroup;
        this.launchTemplate = launchTemplate;
        this.region = region;
    }

    @Override
    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
    
    @Override
    public LaunchTemplate getLaunchTemplate() {
        return launchTemplate;
    }

    @Override
    public Region getRegion() {
        return region;
    }
}
