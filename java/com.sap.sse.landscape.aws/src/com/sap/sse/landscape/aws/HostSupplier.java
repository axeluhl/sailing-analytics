package com.sap.sse.landscape.aws;

import java.net.InetAddress;

import com.sap.sse.common.TimePoint;

@FunctionalInterface
public interface HostSupplier<ShardingKey, HostT extends AwsInstance<ShardingKey>> {
    HostT supply(String instanceId, AwsAvailabilityZone az, InetAddress privateIpAddress, TimePoint launchTimePoint, AwsLandscape<ShardingKey> landscape);
}