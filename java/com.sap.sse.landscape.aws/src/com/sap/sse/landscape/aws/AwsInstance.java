package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

/**
 * An AWS EC2 host that has been created from a {@link MachineImage} that supports deploying an application,
 * has a {@link ReverseProxy} on it
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface AwsInstance<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends Host {
    String getInstanceId();

    void terminate();
}
