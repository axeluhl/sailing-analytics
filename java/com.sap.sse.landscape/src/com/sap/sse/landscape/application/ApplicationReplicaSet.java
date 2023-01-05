package com.sap.sse.landscape.application;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.Release;

/**
 * A replica set with master and zero or more replicas. Temporarily, e.g., during an upgrade procedure, even the master
 * process may disappear. The replica set's name is also considered to be the "server name" of the processes
 * constituting it.
 * 
 * @author Axel Uhl (D043530)
 */
public interface ApplicationReplicaSet<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>> extends Named {
    /**
     * The application version that the nodes in this replica set are currently running. During an
     * {@link #upgrade(Release)} things may temporarily seem inconsistent.
     */
    default Release getVersion(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return getMaster().getVersion(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    /**
     * Upgrades this replica set to a new version. Things may temporarily seem inconsistent; e.g., a master
     * process may be stopped, upgraded to the new version, and then replica processes may be fired up against the new
     * master, and when enough replicas have reached an available state they will replace the previous replicas.
     */
    void upgrade(Release newVersion);
    
    ProcessT getMaster();
    
    Iterable<ProcessT> getReplicas();
    
    default Iterable<ProcessT> getReadyReplicas(Optional<Duration> optionalTimeout) {
        return Util.filter(getReplicas(), r->{
            try {
                return r.isReady(optionalTimeout);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * The HTTP traffic port used by all processes of this application replica set. This must be so because processes
     * may be added as targets to a target group, and such a target group always routes traffic to the same port
     * for all nodes added to it. 
     */
    default int getPort() throws InterruptedException, ExecutionException {
        return getMaster().getPort();
    }
    
    /**
     * @return the scopes that are currently hosted by this application replica set; note that a scope may be "in
     *         transit" which can mean that for parts of the transit time period the scope may be available on two
     *         replica sets (source and target) until the source (exporting) replica set removes the scope again.
     */
    Iterable<Scope<ShardingKey>> getScopes();
    
    /**
     * Moves a {@link Scope} with all its content from {@code source} into this replica set. The process may fail with
     * an exception, e.g., for connectivity or permission reasons, or---if the {@code failUponDiff} parameter is set to
     * {@code true}---for differences found when comparing the result in this replica set with the original content at
     * {@code source}. The {@code removeFromSourceUponSuccess} and {@code setRemoveReferenceInSourceUponSuccess} parameters
     * control how to proceed after successful import.
     * 
     * @see #setRemoteReference
     */
    void importScope(ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> source, Scope<ShardingKey> scopeToImport,
            boolean failUponDiff, boolean removeFromSourceUponSuccess, boolean setRemoveReferenceInSourceUponSuccess);
    
    void removeScope(Scope<ShardingKey> scope);
    
    /**
     * Tells this replica set whether read requests may also be addressed at the master node in case there are one or
     * more {@link #getReplicas() replicas} configured. If setting this to {@code true}, the {@link #getMaster() master
     * process} will be targeted by regular read requests just like any other {@link #getReplicas() replica} will.
     * Otherwise, if one or more replicas are available, the master node will receive only modifying transactions, and
     * reading requests require a replica to be {@link Process#isAlive(Optional) available} in this replica set; if trying
     * to set to {@code false} and no replica is currently available, the method throws an
     * {@link IllegalStateException}.<p>
     * 
     * TODO Maybe this should move to the {@link ApplicationLoadBalancer} interface; otherwise, ApplicationReplicaSet would need to know about the load balancer(s) responsible for it
     * 
     * @throws IllegalStateException
     *             in case {@code readFromMaster} is {@code false} and there is currently no {@link #getReplicas()
     *             replica} currently {@link Process#isReady(Optional<Duration>) ready} to receive requests.
     */
    void setReadFromMaster(boolean readFromMaster) throws IllegalStateException;

    /**
     * See {@link #setReadFromMaster(boolean)}
     */
    boolean isReadFromMaster();

    /**
     * The fully-qualified host name by which this application replica set is publicly reachable. When resolving this
     * hostname through DNS, the result is expected to identify a load balancer which contains the ingress rules for
     * this application replica set.<p>
     * 
     * If we plan to support multi-region distribution of application replica sets in the future, resolving the hostname
     * may have to happen on a per-region basis. It would then be nice if this application replica set would "know" its
     * regions to which it has been deployed.
     */
    String getHostname() throws InterruptedException, ExecutionException;

    /**
     * The "SERVER_NAME" property that is equal for the master and all replica processes of this replica set. It is
     * at the same time the {@link #getName() name} of this application replica set.
     */
    default String getServerName() {
        return getName();
    }
}
