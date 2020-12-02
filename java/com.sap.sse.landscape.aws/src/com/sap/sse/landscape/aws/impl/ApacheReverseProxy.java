package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import com.jcraft.jsch.JSchException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.ssh.SshCommandChannel;

/**
 * An Apache2-based reverse proxy implementation (httpd) that makes specific assumptions about the availability of an
 * {@link AmazonMachineImage} that can be used to launch and configure such a reverse proxy instance on one or more
 * instances running in one or more {@link AwsAvailabilityZone availability zones}.<p>
 * 
 * For each "scope" that has a redirect rule in this reverse proxy, a separate file is maintained under
 * {@code /etc/httpd/conf.d} that is named after the scope, with the usual {@code .conf} suffix such that
 * a {@code systemctl reload httpd} will automatically pick those up.<p>
 * 
 * TODO how do we remember the hosts/instances/nodes/processes that together form this {@link ApacheReverseProxy}? DB Persistence? Tags?
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractApacheReverseProxy<ShardingKey, MetricsT, ProcessT>
implements com.sap.sse.landscape.Process<RotatingFileBasedLog, MetricsT> {
    private static final Logger logger = Logger.getLogger(ApacheReverseProxy.class.getName());
    
    /**
     * five minutes of timeout for most network-related actions
     */
    private static final Optional<Duration> TIMEOUT = Optional.of(Duration.ONE_MINUTE.times(5)); 
    
    /**
     * The configuration directory where files with extension {@link #CONFIG_FILE_EXTENSION} can be placed which
     * a {@code reload} will pick up and evaluate.
     */
    private static final String CONFIG_PATH = "/etc/httpd/conf.d";
    
    /**
     * Extension for files in the {@link #CONFIG_PATH} folder that will automatically be picked up when reloading
     * the proxy's configuration.
     */
    private static final String CONFIG_FILE_EXTENSION = ".conf";
    
    private static final String HOME_REDIRECT_MACRO = "Home-SSL";
    private static final String PLAIN_REDIRECT_MACRO = "Plain-SSL";
    private static final String EVENT_REDIRECT_MACRO = "Event-SSL";
    private static final String SERIES_REDIRECT_MACRO = "Series-SSL";
    
    private final AwsInstance<ShardingKey, MetricsT> host;
    
    public ApacheReverseProxy(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape, AwsInstance<ShardingKey, MetricsT> host) {
        super(landscape);
        this.host = host;
    }
    
    private String getConfigFileNameForScope(Scope<ShardingKey> scope) {
        return scope.toString()+CONFIG_FILE_EXTENSION;
    }

    private String getConfigFileNameForHostname(String hostname) {
        return hostname+CONFIG_FILE_EXTENSION;
    }

    private void setRedirect(String configFileNameForHostname, String macroName, String hostname, String... macroArguments) throws InterruptedException, JSchException, IOException {
        final String command = "echo \"Use "+macroName+" "+hostname+" "+String.join(" ", macroArguments)+"\" >"+getConfigFilePath(configFileNameForHostname)+
                "; service httpd reload";
        final Pair<String, String> stdoutAndStderr = runCommandAndReturnStdoutAndStderr(command);
        logger.info("Standard output from setting up the re-direct for "+hostname+" and reloading the Apache httpd server: "+stdoutAndStderr.getA());
        logger.info("Standard error from setting up the re-direct for "+hostname+" and reloading the Apache httpd server: "+stdoutAndStderr.getB());
    }
    
    private Pair<String, String> runCommandAndReturnStdoutAndStderr(String command) throws IOException, InterruptedException, JSchException {
        final SshCommandChannel sshChannel = getHost().createRootSshChannel(TIMEOUT);
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        sshChannel.sendCommandLineSynchronously(command, stderr);
        final String stdout = sshChannel.getStreamContentsAsString();
        return new Pair<>(stdout, stderr.toString());
    }
    
    private String getConfigFilePath(String configFileNameForHostname) {
        return CONFIG_PATH+"/"+configFileNameForHostname;
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope,ProcessT applicationReplicaSet) {
        // TODO Implement ApacheReverseProxy.setScopeRedirect(...)
    }

    @Override
    public void setPlainRedirect(String hostname, ProcessT applicationProcess) throws InterruptedException, JSchException, IOException {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), PLAIN_REDIRECT_MACRO, hostname, host, ""+port);
    }

    @Override
    public void setHomeRedirect(String hostname, ProcessT applicationProcess) throws InterruptedException, JSchException, IOException {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), HOME_REDIRECT_MACRO, hostname, host, ""+port);
    }

    @Override
    public void setEventRedirect(String hostname, ProcessT applicationProcess, UUID eventId) throws InterruptedException, JSchException, IOException {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), EVENT_REDIRECT_MACRO, hostname, eventId.toString(), host, ""+port);
    }

    @Override
    public void setEventSeriesRedirect(String hostname, ProcessT applicationProcess,
            UUID leaderboardGroupId) throws InterruptedException, JSchException, IOException {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), SERIES_REDIRECT_MACRO, hostname, leaderboardGroupId.toString(), host, ""+port);
    }

    @Override
    public void removeRedirect(Scope<ShardingKey> scope) throws IOException, InterruptedException, JSchException {
        final String configFilePath = getConfigFilePath(getConfigFileNameForScope(scope));
        removeRedirect(configFilePath, scope.toString());
    }
    
    @Override
    public void removeRedirect(String hostname) throws IOException, InterruptedException, JSchException {
        final String configFilePath = getConfigFilePath(getConfigFileNameForHostname(hostname));
        removeRedirect(configFilePath, hostname);
    }
    
    private void removeRedirect(String configFilePath, String redirectNameForLogOutput) throws IOException, InterruptedException, JSchException {
        final String command = "rm "+configFilePath+"; service httpd reload";
        final Pair<String, String> stdoutAndStderr = runCommandAndReturnStdoutAndStderr(command);
        logger.info("Standard output from removing the re-direct for "+redirectNameForLogOutput+" and reloading the Apache httpd server: "+stdoutAndStderr.getA());
        logger.info("Standard error from removing the re-direct for "+redirectNameForLogOutput+" and reloading the Apache httpd server: "+stdoutAndStderr.getB());
    }

    @Override
    public void terminate() {
        getLandscape().terminate(host);
    }

    @Override
    public int getPort() {
        return 443; // TODO currently, we offload SSL only at the reverse proxies; but we should change this to SSL offloading at the load balancer, and then this would have to become 80 (HTTP)
    }

    /**
     * Making things more specific: as we're in the AWS universe here, the {@link Host} returned more specifically is an
     * {@link AwsInstance}.
     */
    @Override
    public AwsInstance<ShardingKey, MetricsT> getHost() {
        return host;
    }

    @Override
    public RotatingFileBasedLog getLog() {
        // TODO Implement Process<LogT,MetricsT>.getLog(...)
        return null;
    }

    @Override
    public MetricsT getMetrics() {
        // TODO Implement Process<LogT,MetricsT>.getMetrics(...)
        return null;
    }

    @Override
    public boolean isReady(Optional<Duration> optionalTimeout) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(getPort() == 443 ? "https" : "http",
                    getHost().getPublicAddress(optionalTimeout).getCanonicalHostName(), getPort(), getHealthCheckPath())
                            .openConnection();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            logger.info("Ready-check failed for "+this+": "+e.getMessage());
            return false;
        }
    }
}