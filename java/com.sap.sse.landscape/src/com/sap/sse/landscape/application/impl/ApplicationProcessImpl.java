package com.sap.sse.landscape.application.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.impl.ProcessImpl;
import com.sap.sse.landscape.impl.ReleaseImpl;
import com.sap.sse.landscape.ssh.SshCommandChannel;

public class ApplicationProcessImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends ProcessImpl<RotatingFileBasedLog, MetricsT>
implements ApplicationProcess<ShardingKey, MetricsT, ProcessT> {
    private static final String ENV_SH = "env.sh";
    private static final String VERSION_TXT = "configuration/jetty/version.txt";
    
    /**
     * Absolute path in the file system of the host on which this process is running and that represents
     * this process's working directory. This directory is expected to contain a file named {@link #ENV_SH}
     * whose contents can be obtained using the {@link #getEnvSh(Optional)} method.
     */
    private final String serverDirectory;

    /**
     * Alternative constructor that doesn't take the port number as argument but instead tries to obtain it from the {@link #ENV_SH env.sh} file
     * located on the {@code host} in the {@code serverDirectory} specified.
     */
    public ApplicationProcessImpl(Host host, String serverDirectory, Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, InterruptedException {
        this(readPortFromDirectory(host, serverDirectory, optionalTimeout), host, serverDirectory);
    }
    
    public ApplicationProcessImpl(int port, Host host, String serverDirectory) {
        super(port, host);
        this.serverDirectory = serverDirectory;
    }

    private static int readPortFromDirectory(Host host, String serverDirectory, Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, InterruptedException {
        return Integer.parseInt(getEnvShValueFor(host, serverDirectory, DefaultProcessConfigurationVariables.SERVER_PORT.name(), optionalTimeout));
    }
    
    @Override
    public Release getRelease(ReleaseRepository releaseRepository, Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException, InterruptedException {
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
    private String getVersionTxt(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException, InterruptedException {
        return getFileContents(getServerDirectory()+"/"+VERSION_TXT, optionalTimeout);
    }

    @Override
    public boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout) {
        // TODO Implement ApplicationProcessImpl.tryCleanShutdown(...)
        return false;
    }
    
    @Override
    public int getTelnetPortToOSGiConsole(Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, SftpException, InterruptedException {
        return Integer.parseInt(getEnvShValueFor(DefaultProcessConfigurationVariables.TELNET_PORT, optionalTimeout));
    }
    
    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable isn't set
     * by evaluating the {@code env.sh} file on the {@link #getHost() host}.
     */
    @Override
    public String getEnvShValueFor(String variableName, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException {
        return getEnvShValueFor(getHost(), getServerDirectory(), variableName, optionalTimeout);
    }
    
    protected static String getEnvShValueFor(Host host, String serverDirectory, String variableName, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException {
        final SshCommandChannel sshChannel = host.createRootSshChannel(optionalTimeout);
        final String variableValue = sshChannel.runCommandAndReturnStdoutAndLogStderr(". "+getEnvShPath(serverDirectory)+">/dev/null 2>/dev/null; "+
                                                "echo \"${"+variableName+"}\"", /* stderr prefix */ null, /* stderr log level */ null);
        return variableValue.endsWith("\n") ? variableValue.substring(0, variableValue.length()-1) : variableValue;
    }
    
    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable cannot be found
     * in the evaluated {@code env.sh} file.
     */
    @Override
    public String getEnvShValueFor(ProcessConfigurationVariable variable, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException {
        return getEnvShValueFor(variable.name(), optionalTimeout);
    }

    @Override
    public String getServerDirectory() {
        return serverDirectory;
    }
    
    private static String getEnvShPath(String serverDirectory) {
        return serverDirectory+"/"+ENV_SH;
        
    }
    private String getEnvShPath() {
        return getEnvShPath(getServerDirectory());
    }

    @Override
    public String getServerName(Optional<Duration> optionalTimeout) throws Exception {
        return getEnvShValueFor(DefaultProcessConfigurationVariables.SERVER_NAME, optionalTimeout);
    }

    @Override
    public String getEnvSh(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException, InterruptedException {
        return getFileContents(getEnvShPath(), optionalTimeout);
    }

    protected String getFileContents(String path, Optional<Duration> optionalTimeout)
            throws JSchException, IOException, SftpException, InterruptedException {
        final ChannelSftp sftpChannel = getHost().createRootSftpChannel(optionalTimeout);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sftpChannel.connect((int) optionalTimeout.orElse(Duration.NULL).asMillis()); 
        sftpChannel.get(path, bos);
        sftpChannel.disconnect();
        return bos.toString();
    }
    
    /**
     * No good health check path known for arbitrary process; returning {@code "/"} as a default value.
     */
    @Override
    public String getHealthCheckPath() {
        return "/";
    }
    
    protected JSONObject getReplicationStatus(Optional<Duration> optionalTimeout)
            throws ClientProtocolException, IOException, ParseException {
        return getReplicationStatus(getReplicationStatusPostUrlAndQuery(optionalTimeout));
    }

    private JSONObject getReplicationStatus(final URL url)
            throws IOException, ClientProtocolException, ParseException {
        final HttpPost postRequest = new HttpPost(url.toString());
        final HttpClient client = HttpClientBuilder.create().build();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        client.execute(postRequest).getEntity().writeTo(bos);
        return (JSONObject) new JSONParser().parse(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
    }

    @Override
    public String getMasterServerName(Optional<Duration> optionalTimeout) throws ClientProtocolException, IOException, ParseException, InterruptedException {
        String result = null;
        boolean obtainedReplicationStatusSuccessfully = false;
        final TimePoint start = TimePoint.now();
        while (!obtainedReplicationStatusSuccessfully && optionalTimeout.map(d->start.until(TimePoint.now()).compareTo(d) < 0).orElse(true)) {
            try {
                final JSONObject replicationStatus = getReplicationStatus(optionalTimeout);
                obtainedReplicationStatusSuccessfully = true;
                for (final Object replicable : (JSONArray) replicationStatus.get("replicables")) {
                    final JSONObject replicableAsJson = (JSONObject) replicable;
                    if (replicableAsJson.get("replicatedfrom") != null) {
                        final JSONObject replicatedFrom = (JSONObject) replicableAsJson.get("replicatedfrom");
                        final String hostname = replicatedFrom.get("hostname").toString();
                        final int port = ((Number) replicatedFrom.get("port")).intValue();
                        // the bearer token that is good for the connection to this process instance should then also
                        // be recognized as valid on this instance's master and have the READ_REPLICATOR permission for the same server name
                        final JSONObject masterReplicationStatus = getReplicationStatus(optionalTimeout, hostname, port);
                        result = masterReplicationStatus.get("servername").toString();
                        break;
                    }
                }
            } catch (Exception e) {
                logger.info("Exception waiting for server name."+optionalTimeout.map(d->" Waiting another "+d.minus(start.until(TimePoint.now())).toString()).orElse(""));
                Thread.sleep(Duration.ONE_SECOND.times(5).asMillis());
            }
        }
        return result;
    }

    private JSONObject getReplicationStatus(Optional<Duration> optionalTimeout, String hostname, int port)
            throws ClientProtocolException, IOException, ParseException {
        return getReplicationStatus(getReplicationStatusPostUrlAndQuery(hostname, port));
    }
}
