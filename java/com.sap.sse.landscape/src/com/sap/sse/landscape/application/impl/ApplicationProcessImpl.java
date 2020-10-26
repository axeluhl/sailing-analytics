package com.sap.sse.landscape.application.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.impl.ProcessImpl;
import com.sap.sse.landscape.impl.ReleaseImpl;
import com.sap.sse.landscape.ssh.SshCommandChannel;

public class ApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
        extends ProcessImpl<RotatingFileBasedLog, MetricsT>
        implements ApplicationProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    
    private static final String ENV_SH = "env.sh";
    private static final String VERSION_TXT = "configuration/jetty/version.txt";
    
    /**
     * Absolute path in the file system of the host on which this process is running and that represents
     * this process's working directory. This directory is expected to contain a file named {@link #ENV_SH}
     * whose contents can be obtained using the {@link #getEnvSh(Optional)} method.
     */
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
    public Release getRelease(ReleaseRepository releaseRepository, Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException {
        final Pattern pattern = Pattern.compile("^[^-]*-([^ ]*) System:");
        final Matcher matcher = pattern.matcher(getVersionTxt(optionalTimeout));
        final Release result;
        if (matcher.find()) {
            result = new ReleaseImpl(matcher.group(1), releaseRepository);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Under the path {@code configuration/jetty/version.txt} each release is expected to present a version string in the
     * format <tt>{commitId}-{releaseName} System: {mongoDbUriWithoutSchemaOrHostPortDBName}-{expeditionPort}-{replicationHost}:{replicationPort}/{replicationChannel} Started: {yyyyMMddHHmm}</tt>.
     * With this it is possible to infer the release that will be run upon the next process start, which is also the
     * one running now if this process is currently running and no other release has been deployed since the process
     * has started.
     */
    private String getVersionTxt(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException {
        return getFileContents(getServerDirectory()+"/"+VERSION_TXT, optionalTimeout);
    }

    @Override
    public boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout) {
        // TODO Implement ApplicationProcessImpl.tryCleanShutdown(...)
        return false;
    }
    
    @Override
    public int getTelnetPortToOSGiConsole(Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, SftpException, InterruptedException {
        return Integer.parseInt(getEnvShValueFor(getEnvSh(optionalTimeout), ProcessConfigurationVariable.TELNET_PORT, optionalTimeout));
    }
    
    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable cannot be found
     * in the {@code envShContents} string passed or that string was {@code null}.
     */
    protected String getEnvShValueFor(String envShContents, String variableName, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException {
        final SshCommandChannel sshChannel = getHost().createRootSshChannel(optionalTimeout);
        sshChannel.sendCommandLineSynchronously(". "+getEnvShPath()+">/dev/null 2>/dev/null; echo \"${"+variableName+"}\"", /* stderr */ new ByteArrayOutputStream());
        final String variableValue = sshChannel.getStreamContentsAsString();
        return variableValue.endsWith("\n") ? variableValue.substring(0, variableValue.length()-1) : variableValue;
    }
    
    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable cannot be found
     * in the {@code envShContents} string passed or that string was {@code null}.
     */
    protected String getEnvShValueFor(String envShContents, ProcessConfigurationVariable variable, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException {
        return getEnvShValueFor(envShContents, variable.name(), optionalTimeout);
    }

    @Override
    public String getServerDirectory() {
        return serverDirectory;
    }
    
    private String getEnvShPath() {
        return getServerDirectory()+"/"+ENV_SH;
    }

    @Override
    public String getServerName() {
        // TODO Implement ApplicationProcessImpl.getServerName(...)
        return null;
    }

    @Override
    public String getEnvSh(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException {
        return getFileContents(getEnvShPath(), optionalTimeout);
    }

    protected String getFileContents(String path, Optional<Duration> optionalTimeout)
            throws JSchException, IOException, SftpException {
        final ChannelSftp sftpChannel = getHost().createRootSftpChannel(optionalTimeout);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sftpChannel.connect((int) optionalTimeout.orElse(Duration.NULL).asMillis()); 
        sftpChannel.get(path, bos);
        return bos.toString();
    }

    
    /**
     * No health check path known for arbitrary process; returning {@code null} as a default value.
     */
    @Override
    public String getHealthCheckPath() {
        return null;
    }

    /**
     * Without knowledge about how to do an availability check, we report {@code true} as the default result.
     */
    @Override
    public boolean isReady(Optional<Duration> optionalTimeout) throws MalformedURLException, IOException {
        return true;
    }
}
