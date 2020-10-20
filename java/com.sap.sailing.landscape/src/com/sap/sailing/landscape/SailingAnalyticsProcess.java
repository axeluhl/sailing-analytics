package com.sap.sailing.landscape;

import com.jcraft.jsch.ChannelSftp;
import com.sap.sse.landscape.application.ApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends
        ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    int getTelnetPortToOSGiConsole();
    
    int getExpeditionUdpPort();
    
    /**
     * @return the directory as an absolute path that can be used, e.g., in a {@link ChannelSftp} to change directory to
     *         it or to copy files to or read files from there.
     */
    String getServerDirectory();
    
    String getEnvSh();
}
