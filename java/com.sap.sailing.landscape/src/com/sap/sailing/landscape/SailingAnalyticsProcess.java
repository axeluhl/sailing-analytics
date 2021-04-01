package com.sap.sailing.landscape;

import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.aws.AwsLandscape;

public interface SailingAnalyticsProcess<ShardingKey> extends ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    static Logger logger = Logger.getLogger(SailingAnalyticsProcess.class.getName());
    static String HEALTH_CHECK_PATH = "/gwt/status";

    int getExpeditionUdpPort(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
    
    @Override
    SailingAnalyticsHost<ShardingKey> getHost();
    
    /**
     * First, {@link #tryShutdown(Optional, Optional, byte[]) shuts this process down}. Then, the process directory will
     * be removed. If, according to
     * {@link #getHost()}.{@link SailingAnalyticsHost#getApplicationProcesses(Optional, Optional, byte[])
     * getApplicationProcesses(...)} there are no other processes deployed on the {@link #getHost() host}, the host is
     * {@link AwsLandscape#terminate(com.sap.sse.landscape.aws.AwsInstance) terminated}.
     */
    void stopAndTerminateIfLast(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase);

}
