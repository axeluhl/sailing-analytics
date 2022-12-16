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

    @Override
    AwsInstance<ShardingKey> getHost();
    
    /**
     * First, {@link #tryShutdown(Optional, Optional, byte[]) shuts this process down}. Then, the process directory will
     * be removed. If, according to
     * {@link #getHost()}.{@link SailingAnalyticsHost#getApplicationProcesses(Optional, Optional, byte[])
     * getApplicationProcesses(...)} there are no other processes deployed on the {@link #getHost() host}, the host is
     * {@link AwsLandscape#terminate(com.sap.sse.landscape.aws.AwsInstance) terminated}.
     */
    void stopAndTerminateIfLast(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase);
}
