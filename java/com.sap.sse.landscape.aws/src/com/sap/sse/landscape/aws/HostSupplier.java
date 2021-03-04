package com.sap.sse.landscape.aws;

import java.net.InetAddress;

@FunctionalInterface
public interface HostSupplier<ShardingKey, HostT extends AwsInstance<ShardingKey>> {
    HostT supply(String instanceId, AwsAvailabilityZone az, InetAddress privateIpAddress, AwsLandscape<ShardingKey> landscape);
}