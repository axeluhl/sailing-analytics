package com.sap.sse.landscape.application;

import java.io.IOException;
import java.util.Optional;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.mongodb.Database;

public interface ApplicationProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
        extends Process<RotatingFileBasedLog, MetricsT> {
    /**
     * @return the replica set to which this process belongs<p>
     * 
     *         TODO define this more precisely; an instance can be replica with regards to the SecurityService and
     *         SharedSailingData replicables and at the same time be master for RacingEventService and all the other
     *         replicables. What then is "the" replica set of the process?
     */
    ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getReplicaSet();
    
    /**
     * @return the release that this process is currently running
     */
    Release getRelease();
    
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
    
    int getTelnetPortToOSGiConsole();

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
    String getServerName();
    
    String getEnvSh(Optional<Duration> optionalTimeout) throws JSchException, IOException, SftpException;
}
