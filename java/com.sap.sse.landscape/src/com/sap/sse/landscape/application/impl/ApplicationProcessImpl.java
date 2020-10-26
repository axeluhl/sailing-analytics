package com.sap.sse.landscape.application.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.impl.ProcessImpl;

public class ApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
        extends ProcessImpl<RotatingFileBasedLog, MetricsT>
        implements ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    
    private static final String ENV_SH = "env.sh";
    private static final String DEFAULT_SERVER_DIRECTORY = "server";
    private final String serverDirectory;

    public ApplicationProcessImpl(int port, Host host, String serverDirectory) {
        super(port, host);
        this.serverDirectory = serverDirectory;
    }

    @Override
    public ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getReplicaSet() {
        // TODO Implement ApplicationProcessImpl.getReplicaSet(...)
        return null;
    }

    @Override
    public Release getRelease() {
        // TODO Implement ApplicationProcessImpl.getRelease(...)
        return null;
    }

    @Override
    public boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout) {
        // TODO Implement ApplicationProcessImpl.tryCleanShutdown(...)
        return false;
    }
    
    @Override
    public int getTelnetPortToOSGiConsole() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getTelnetPortToOSGiConsole(...)
        return 0;
    }
    
    @Override
    public String getServerDirectory() {
        // TODO Implement SailingAnalyticsProcess<ShardingKey>.getServerDirectory(...)
        // TODO does this have to be a constructor parameter, or do we want to make it discoverable through the REST API, e.g., /gwt/status?
        return serverDirectory;
    }

    @Override
    public String getServerName() {
        return DEFAULT_SERVER_DIRECTORY;
    }

    @Override
    public String getEnvSh(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException {
        final ChannelSftp sftpChannel = getHost().createRootSftpChannel(optionalTimeout);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sftpChannel.connect((int) optionalTimeout.orElse(Duration.NULL).asMillis()); 
        sftpChannel.get(getServerDirectory()+"/"+ENV_SH, bos);
        return bos.toString();
    }

    /**
     * Without knowledge about how to do an availability check, we report {@code true} as the default result.
     */
    @Override
    public boolean isReady() {
        return true;
    }
}
