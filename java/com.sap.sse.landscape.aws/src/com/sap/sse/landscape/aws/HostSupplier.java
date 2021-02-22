package com.sap.sse.landscape.aws;

@FunctionalInterface
public interface HostSupplier<ShardingKey, HostT extends AwsInstance<ShardingKey>> {
    HostT supply(String instanceId, AwsAvailabilityZone az, AwsLandscape<ShardingKey> landscape);
}