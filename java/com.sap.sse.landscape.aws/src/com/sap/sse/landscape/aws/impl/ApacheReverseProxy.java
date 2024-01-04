package com.sap.sse.landscape.aws.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
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
     * The configuration directory within the "httpd/config" git repo where files with extension {@link #CONFIG_FILE_EXTENSION} can be placed which
     * a {@code reload} will pick up and evaluate.
     */
    private static final String RELATIVE_CONFIG_PATH = "conf.d";
    
    /**
     * The path to the "httpd/config" git repo which stores all httpd configuration files.
     */
    private static final String CONFIG_REPO_PATH = "/etc/httpd";
    
    private static final String CONFIG_REPO_MAIN_BRANCH_NAME = "main";
    
    /**
     * Extension for files in the {@link #CONFIG_PATH} folder that will automatically be picked up when reloading
     * the proxy's configuration.
     */
    private static final String CONFIG_FILE_EXTENSION = ".conf";
    
    private static final String HOME_REDIRECT_MACRO = "Home-SSL";
    private static final String PLAIN_REDIRECT_MACRO = "Plain-SSL";
    private static final String EVENT_REDIRECT_MACRO = "Event-SSL";
    private static final String SERIES_REDIRECT_MACRO = "Series-SSL";
    private static final String HOME_ARCHIVE_REDIRECT_MACRO = "Home-ARCHIVE";
    private static final String EVENT_ARCHIVE_REDIRECT_MACRO = "Event-ARCHIVE";
    private static final String SERIES_ARCHIVE_REDIRECT_MACRO = "Series-ARCHIVE";
    private static final String STATUS = "Status";
    private static final String CONFIG_FILE_FOR_INTERNALS = "001-internals"+CONFIG_FILE_EXTENSION;
    
    private final AwsInstance<ShardingKey> host;
    
    public ApacheReverseProxy(AwsLandscape<ShardingKey> landscape, AwsInstance<ShardingKey> host) {
        super(landscape);
        this.host = host;
    }
    
    private String getConfigFileNameForScope(Scope<ShardingKey> scope) {
        return scope.toString() + CONFIG_FILE_EXTENSION;
    }

    private String getConfigFileNameForHostname(String hostname) {
        return hostname + CONFIG_FILE_EXTENSION;
    }
    
    public void rotateLogs(Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String command = "logrotate --force /etc/logrotate.conf";
        logger.info("Standard output from forced log rotate on" + this.getHostname() + ": " + runCommandAndReturnStdoutAndStderr(command, "Standard error fromlogrotate ",
                        Level.INFO, optionalKeyName, privateKeyEncryptionPassphrase));
    }
    
    private void setRedirect(String configFileNameForHostname, String macroName, String hostname,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase, String... macroArguments)
            throws Exception {
        final String command = "echo \"Use " + macroName + " " + hostname + " " + String.join(" ", macroArguments)
                + "\" > " + getAbsoluteConfigFilePath(configFileNameForHostname) + "; service httpd reload && "
                + "cd " + CONFIG_REPO_PATH + " && " + createCommitAndPushString(configFileNameForHostname, "\"Set configFileNameForHostname redirect\"", false) ;
        logger.info("Standard output from setting up the re-direct for " + hostname
                + " and reloading the Apache httpd server: "
                + runCommandAndReturnStdoutAndStderr(command,
                        "Standard error from setting up the re-direct for " + hostname
                                + " and reloading the Apache httpd server: ",
                        Level.INFO, optionalKeyName, privateKeyEncryptionPassphrase));
    }
    
    private String runCommandAndReturnStdoutAndStderr(String command, String stderrLogPrefix, Level stderrLogLevel, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final SshCommandChannel sshChannel = getHost().createRootSshChannel(TIMEOUT, optionalKeyName, privateKeyEncryptionPassphrase);
        final String stdout = sshChannel.runCommandAndReturnStdoutAndLogStderr(command, stderrLogPrefix, stderrLogLevel);
        return stdout;
    }
    
    /**
     *  Creates a string that can be ran on an instance to commit and optionally push changes to a file (within a git repo). Assumes the command is ran from within the repository.
     * @param editedFileName The file name edited, created or deleted to commit.
     * @param commitMsg The commit message. Make sure it is enclosed with escaped speech marks.
     * @param performPush Boolean indicating whether to push changes or not.
     * @return Returns the created string command to perform a commit and optional push.
     */
    private String createCommitAndPushString(String editedFileName, String commitMsg, boolean performPush) {
        StringBuilder command = new StringBuilder(" git add " + getRelativeConfigFilePath(editedFileName) + " && git commit -m " + commitMsg); // space at beginning added for safety
        if (performPush) {
            command.append(" && git push origin " + CONFIG_REPO_MAIN_BRANCH_NAME);
        }
        command.append(";");
        return command.toString();
    }
    
    /**
     * 
     * @param configFileNameForHostname The name of the file to append to the relative path. 
     * @return Returns the relative path. This is the path, within the directory specified by {@link #CONFIG_REPO_PATH}, to where the argument file is or may be.
     */
    private String getRelativeConfigFilePath(String configFileNameForHostname) {
        return RELATIVE_CONFIG_PATH + "/" + configFileNameForHostname;
    }
    
   /**
    * 
    * @param configFileNameForHostname The filename to append to the absolute path.
    * @return Returns the absolute path to the config file passed as an argument (which may be for creation, deletion or just finding the file).
    */
    private String getAbsoluteConfigFilePath(String configFileNameForHostname) {
        return CONFIG_REPO_PATH + "/" + RELATIVE_CONFIG_PATH + "/" + configFileNameForHostname;
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope, ProcessT applicationReplicaSet) {
        // TODO Implement ApacheReverseProxy.setScopeRedirect(...)
    }

    @Override
    public void setPlainRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), PLAIN_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, host, ""+port);
    }

    @Override
    public void setHomeRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), HOME_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, host, ""+port);
    }

    @Override
    public void setEventRedirect(String hostname, ProcessT applicationProcess, UUID eventId, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), EVENT_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, eventId.toString(), host, ""+port);
    }

    @Override
    public void setEventSeriesRedirect(String hostname, ProcessT applicationProcess,
            UUID leaderboardGroupId, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String host = applicationProcess.getHost().getPrivateAddress().getHostAddress();
        final int port = applicationProcess.getPort();
        setRedirect(getConfigFileNameForHostname(hostname), SERIES_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, leaderboardGroupId.toString(), host, ""+port);
    }

    @Override
    public void setHomeArchiveRedirect(String hostname, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(getConfigFileNameForHostname(hostname), HOME_ARCHIVE_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase);
    }

    @Override
    public void setEventArchiveRedirect(String hostname, UUID eventId, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(getConfigFileNameForHostname(hostname), EVENT_ARCHIVE_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, eventId.toString());
    }

    @Override
    public void setEventSeriesArchiveRedirect(String hostname, UUID leaderboardGroupId,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(getConfigFileNameForHostname(hostname), SERIES_ARCHIVE_REDIRECT_MACRO, hostname, optionalKeyName, privateKeyEncryptionPassphrase, leaderboardGroupId.toString());
    }

    @Override
    public void createInternalStatusRedirect(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(CONFIG_FILE_FOR_INTERNALS, STATUS, getHost().getPrivateAddress(optionalTimeout).getHostAddress(), optionalKeyName, privateKeyEncryptionPassphrase, INTERNAL_SERVER_STATUS);
    }

    @Override
    public void removeRedirect(Scope<ShardingKey> scope, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String configFilePath = getAbsoluteConfigFilePath(getConfigFileNameForScope(scope));
        removeRedirect(configFilePath, scope.toString(), optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    @Override
    public void removeRedirect(String hostname, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String configFilePath = getAbsoluteConfigFilePath(getConfigFileNameForHostname(hostname));
        removeRedirect(configFilePath, hostname, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    private void removeRedirect(String configFilePath, String redirectNameForLogOutput,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String command = "rm " + configFilePath + "; service httpd reload";
        logger.info("Standard output from removing the re-direct for " + redirectNameForLogOutput
                + " and reloading the Apache httpd server: "
                + runCommandAndReturnStdoutAndStderr(command,
                        "Standard error from removing the re-direct for " + redirectNameForLogOutput
                                + " and reloading the Apache httpd server: ",
                        Level.INFO, optionalKeyName, privateKeyEncryptionPassphrase));
    }

    @Override
    public void terminate() {
        getLandscape().terminate(host);
    }

    @Override
    public int getPort() {
        return 80;
    }

    /**
     * Making things more specific: as we're in the AWS universe here, the {@link Host} returned more specifically is an
     * {@link AwsInstance}.
     */
    @Override
    public AwsInstance<ShardingKey> getHost() {
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

    /**
     * Restarts the httpd service on the proxy via stop and then start.
     * @param optionalKeyName The keyname to use to connect, via ssh, to the instance to run the necessary commands.
     * @param privateKeyEncryptionPassphrase The passphrase for the key.
     */
    public void restart(Optional<String> optionalKeyName,byte[] privateKeyEncryptionPassphrase) throws Exception {
        String command = "service httpd stop; service httpd start";
        runCommandAndReturnStdoutAndStderr(command, "Restarting Apache httpd server", Level.INFO, optionalKeyName,
                privateKeyEncryptionPassphrase);
    }
}