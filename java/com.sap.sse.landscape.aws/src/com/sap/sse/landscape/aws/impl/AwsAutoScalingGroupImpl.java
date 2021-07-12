package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;

public class AwsAutoScalingGroupImpl implements AwsAutoScalingGroup {
    private static final long serialVersionUID = -6244055510377604441L;
    private final AutoScalingGroup autoScalingGroup;
    private final LaunchConfiguration launchConfiguration;
    private final Region region;

    public AwsAutoScalingGroupImpl(AutoScalingGroup autoScalingGroup, LaunchConfiguration launchConfiguration, Region region) {
        super();
        this.autoScalingGroup = autoScalingGroup;
        this.launchConfiguration = launchConfiguration;
        this.region = region;
    }

    @Override
    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
    
    @Override
    public LaunchConfiguration getLaunchConfiguration() {
        return launchConfiguration;
    }

    @Override
    public Region getRegion() {
        return region;
    }
}
