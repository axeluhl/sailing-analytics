package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.AwsAutoScalingGroup;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;

public class AwsAutoScalingGroupImpl implements AwsAutoScalingGroup {
    private final AutoScalingGroup autoScalingGroup;

    public AwsAutoScalingGroupImpl(AutoScalingGroup autoScalingGroup) {
        super();
        this.autoScalingGroup = autoScalingGroup;
    }

    @Override
    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
}
