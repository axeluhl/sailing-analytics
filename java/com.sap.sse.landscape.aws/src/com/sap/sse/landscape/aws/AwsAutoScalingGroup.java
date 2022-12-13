package com.sap.sse.landscape.aws;

import com.sap.sse.common.Named;
import com.sap.sse.landscape.Region;

import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;

/**
 * Wraps an {@link AutoScalingGroup} and offers convenience methods that are aware of the procedures
 * filling the launch configuration's user data, including aspects such as the release and the image
 * to use.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface AwsAutoScalingGroup extends Named {
    /**
     * Describes on how many requests per minute a new instance gets created.
     */
    public static int DEFAULT_MAX_REQUESTS_PER_TARGET = 15000;
    AutoScalingGroup getAutoScalingGroup();
    
    LaunchConfiguration getLaunchConfiguration();

    Region getRegion();
    
    default String getName() {
        return getAutoScalingGroup().autoScalingGroupName();
    }
}
