package com.sap.sse.landscape.aws.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;

public class ApplicationProcessHostImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AwsInstanceImpl<ShardingKey, MetricsT>
implements ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(ApplicationProcessHostImpl.class.getName());
    private final BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory;

    public ApplicationProcessHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            AwsLandscape<ShardingKey, MetricsT, ?> landscape, BiFunction<Host, String, ProcessT> processFactoryFromHostAndServerDirectory) {
        super(instanceId, availabilityZone, landscape);
        this.processFactoryFromHostAndServerDirectory = processFactoryFromHostAndServerDirectory;
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        @SuppressWarnings("unchecked")
        final AwsLandscape<ShardingKey, MetricsT, ProcessT> castLandscape =
            (AwsLandscape<ShardingKey, MetricsT, ProcessT>) super.getLandscape();
        return castLandscape;
    }

    @Override
    public ReverseProxy<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> getReverseProxy() {
        return new ApacheReverseProxy<>(getLandscape(), this);
    }

    /**
     * The implementation scans the {@link ApplicationProcessHost#DEFAULT_SERVERS_PATH application server deployment
     * folder} for sub-folders. In those sub-folders, the configuration file is analyzed for the port number to instantiate
     * an {@link ApplicationProcess} object for each one.
     */
    @Override
    public Iterable<ProcessT> getApplicationProcesses(Optional<Duration> optionalTimeout) throws SftpException, JSchException, IOException, InterruptedException {
        final Set<ProcessT> result = new HashSet<>();
        final ChannelSftp sftpChannel = createRootSftpChannel(optionalTimeout);
        if (optionalTimeout.isPresent()) {
            sftpChannel.connect((int) optionalTimeout.get().asMillis());
        } else {
            sftpChannel.connect();
        }
        @SuppressWarnings("unchecked")
        final Vector<LsEntry> files = sftpChannel.ls(DEFAULT_SERVERS_PATH);
        for (final LsEntry subdirCandidate : files) {
            if (subdirCandidate.getAttrs().isDir() && !subdirCandidate.getFilename().startsWith(".")) {
                ProcessT process;
                final String serverDirectory = DEFAULT_SERVERS_PATH+"/"+subdirCandidate.getFilename();
                try {
                    process = processFactoryFromHostAndServerDirectory.apply(this, serverDirectory);
                    result.add(process);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Problem creating application process from directory "+serverDirectory+" on host "+this+"; skipping", e);
                }
            }
        }
        sftpChannel.disconnect();
        return result;
    }
}
