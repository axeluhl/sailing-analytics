package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.Region;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;

/**
 * Wraps an {@link AutoScalingGroup} and offers convenience methods that are aware of the procedures
 * filling the launch configuration's user data, including aspects such as the release and the image
 * to use.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface AwsAutoScalingGroup {
    AutoScalingGroup getAutoScalingGroup();

    Region getRegion();
}
