package com.sap.sse.landscape.application;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.mongodb.Database;

public interface ApplicationProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends Process<RotatingFileBasedLog, MetricsT> {
    static Logger logger = Logger.getLogger(ApplicationProcess.class.getName());
    static String REPLICATION_STATUS_POST_URL_PATH_AND_QUERY = "/replication/replication?action=STATUS";
    
    /**
     * @param releaseRepository
     *            mandatory parameter required to enable resolving the release and enabling the download of its
     *            artifacts, including its release notes
     * @return the release that this process is currently running
     */
    Release getRelease(ReleaseRepository releaseRepository, Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException, InterruptedException;
    
    /**
     * Tries to shut down an OSGi application server process cleanly by sending the "shutdown" OSGi command to this
     * process's OSGi console using the {@link #getTelnetPortToOSGiConsole() telnet port}. If the instance hasn't
     * terminated after {@code timeout} after having received this shutdown request, if {@code forceAfterTimeout} is
     * {@code true}, a hard kill command will be used terminate the virtual machine and {@code false} is returned;
     * otherwise ({@code forceAfterTimeout==false}), {@code false} will be returned after the timeout period.
     * 
     * @return {@code true} if the clean shutdown has succeeded, {@code false} otherwise. Note that therefore the result
     *         does not indicate whether the process was finally gone; with {@code forceAfterTimeout==true} callers can
     *         assume that no matter what the result of this call, the VM will finally be gone, but with this logic it's
     *         possible even with a hard shutdown to figure out that a hard shutdown was actually required and the clean
     *         shutdown didn't work.
     */
    boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout);
    
    int getTelnetPortToOSGiConsole(Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, SftpException, InterruptedException;

    /**
     * @return the directory as an absolute path that can be used, e.g., in a {@link ChannelSftp} to change directory to
     *         it or to copy files to or read files from there.
     */
    String getServerDirectory();
    
    /**
     * The name that is the basis for the user group name; e.g., a server named "my" will by default be owned by a
     * dedicated user group named "my-server". For multi-instance servers, a default setup will use this server name also
     * as the base name of the {@link #getServerDirectory() server's directory}. Often, it is also used as the name of
     * the {@link Database}, at least when this is a master node, and the name of the RabbitMQ fan-out exchange used
     * for replication.
     */
    String getServerName(Optional<Duration> optionalTimeout) throws Exception;
    
    String getEnvSh(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException, InterruptedException;

    /**
     * The URL path (everything following the hostname and starting with "/" but without any fragment) that can be
     * appended to the protocol, hostname and port specification in order to produce a full health check URL. The
     * health check URL, when connected to, is expected to return a 200 response if the service is healthy, and anything
     * else in case it's not.
     */
    String getHealthCheckPath();

    String getMasterServerName(Optional<Duration> optionalTimeout) throws Exception;

    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable cannot be found
     * in the evaluated {@code env.sh} file.
     */
    String getEnvShValueFor(ProcessConfigurationVariable variable, Optional<Duration> optionalTimeout)
            throws JSchException, IOException, InterruptedException;

    /**
     * Obtains the last definition of the process configuration variable specified, or {@code null} if that variable isn't set
     * by evaluating the {@code env.sh} file on the {@link #getHost() host}.
     */
    String getEnvShValueFor(String variableName, Optional<Duration> optionalTimeout)
            throws JSchException, IOException, InterruptedException;

    /**
     * Tells whether this process is ready to accept requests. Use this for a health check in a target group
     * that decides whether traffic will be sent to this process. {@link #isReady(Optional<Duration>)} implies {@link #isAlive(Optional)}.
     */
    default boolean isReady(Optional<Duration> optionalTimeout) throws IOException {
        try {
            final HttpURLConnection connection = (HttpURLConnection) getHealthCheckUrl(optionalTimeout)
                            .openConnection();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            logger.info("Ready-check failed for "+this+": "+e.getMessage());
            return false;
        }
    }

    default URL getHealthCheckUrl(Optional<Duration> optionalTimeout) throws MalformedURLException {
        return getUrl(getHealthCheckPath(), optionalTimeout);
    }
    
    default URL getReplicationStatusPostUrlAndQuery(Optional<Duration> optionalTimeout) throws MalformedURLException {
        return getUrl(REPLICATION_STATUS_POST_URL_PATH_AND_QUERY, optionalTimeout);
    }
    
    default URL getReplicationStatusPostUrlAndQuery(String hostname, int port) throws MalformedURLException {
        return new URL("http", hostname, port, REPLICATION_STATUS_POST_URL_PATH_AND_QUERY);
    }
    
    default URL getUrl(String pathAndQuery, Optional<Duration> optionalTimeout) throws MalformedURLException {
        return new URL("http", getHost().getPublicAddress(optionalTimeout).getCanonicalHostName(), getPort(), pathAndQuery);
    }
    
    default boolean waitUntilReady(Optional<Duration> optionalTimeout) throws IOException, InterruptedException {
        final TimePoint startingToPollForReady = TimePoint.now();
        while (!isReady(optionalTimeout) && (!optionalTimeout.isPresent() || startingToPollForReady.until(TimePoint.now()).compareTo(optionalTimeout.get()) <= 0)) {
            if (optionalTimeout.isPresent()) {
                logger.info(""+this+" not yet ready; waiting at most "+TimePoint.now().until(startingToPollForReady.plus(optionalTimeout.get()))+
                        " until "+startingToPollForReady.plus(optionalTimeout.get()));
            } else {
                logger.info(""+this+" not yet ready; waiting forever...");
            }
            Thread.sleep(5000);
        }
        return isReady(optionalTimeout);
    }
}
