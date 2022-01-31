package com.sap.sailing.landscape;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.jcraft.jsch.JSchException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AwsApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends AwsApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    static Logger logger = Logger.getLogger(SailingAnalyticsProcess.class.getName());
    static String HEALTH_CHECK_PATH = "/gwt/status";

    int getExpeditionUdpPort(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
    
    @Override
    SailingAnalyticsHost<ShardingKey> getHost();
    
    /**
     * Refreshes this process to the {@code release} specified. This happens by connecting to the {@link #getHost() instance} by
     * SSH, changing into the {@link #getServerDirectory(Optional) server directory} for this process, running the {@code refreshInstance.sh}
     * script there with the {@code install-release} subcommand parameterized with the {@code release} to install, and then
     * runs {@code ./stop; ./start} to activate the {@code release}.
     */
    void refreshToRelease(Release release, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws IOException, InterruptedException, JSchException, Exception;

}
