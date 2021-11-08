package com.sap.sse.landscape.aws;

import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.mongodb.Database;

public interface AwsApplicationProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ApplicationProcess<ShardingKey, MetricsT, ProcessT> {
    <HostT extends AwsInstance<ShardingKey>> ProcessT getMaster(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) throws Exception;
    
    <HostT extends AwsInstance<ShardingKey>> Iterable<ProcessT> getReplicas(Optional<Duration> optionalTimeout, HostSupplier<ShardingKey, HostT> hostSupplier,
            ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactory) throws Exception;
    
    Database getDatabaseConfiguration(Region region, Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
}
