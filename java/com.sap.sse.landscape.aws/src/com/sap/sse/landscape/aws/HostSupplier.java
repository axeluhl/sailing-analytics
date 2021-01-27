package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

@FunctionalInterface
public interface HostSupplier<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>> {
    HostT supply(String instanceId, AwsAvailabilityZone az, AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape);
}