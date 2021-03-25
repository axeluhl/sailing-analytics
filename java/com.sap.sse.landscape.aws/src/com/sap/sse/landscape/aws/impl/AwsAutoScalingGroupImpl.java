package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;

public class AwsAutoScalingGroupImpl implements AwsAutoScalingGroup {
    private final AutoScalingGroup autoScalingGroup;
    private final Region region;

    public AwsAutoScalingGroupImpl(AutoScalingGroup autoScalingGroup, Region region) {
        super();
        this.autoScalingGroup = autoScalingGroup;
        this.region = region;
    }

    @Override
    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
    
    @Override
    public Region getRegion() {
        return region;
    }
}
