package com.sap.sse.landscape.application;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Process;

public interface ApplicationReplicaSet<ShardingKey, MetricsT extends ApplicationProcessMetrics> {
    /**
     * The application version that the nodes in this replica set are currently running. During an
     * {@link #upgrade(ApplicationVersion)} things may temporarily seem inconsistent.
     */
    ApplicationVersion getVersion();
    
    /**
     * Upgrades this replica set to a new version. Things may temporarily seem inconsistent; e.g., a master
     * process may be stopped, upgraded to the new version, and then replica processes may be fired up against the new
     * master, and when enough replicas have reached an available state they will replace the previous replicas.
     */
    void upgrade(ApplicationVersion newVersion);
    
    ApplicationMasterProcess<ShardingKey, MetricsT> getMaster();
    
    Iterable<ApplicationReplicaProcess<ShardingKey, MetricsT>> getReplicas();
    
    default Iterable<ApplicationReplicaProcess<ShardingKey, MetricsT>> getAvailableReplicas() {
        return Util.filter(getReplicas(), r->r.isAvailable());
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
     * {@code true}--- for differences found when comparing the result in this replica set with the original content at
     * {@code source}. The {@code removeFromSourceUponSuccess} and {@code setRemoveReferenceInSourceUponSuccess} parameters
     * control how to proceed after successful import.
     * 
     * @see #setRemoteReference
     */
    void importScope(ApplicationReplicaSet<ShardingKey, MetricsT> source, Scope<ShardingKey> scopeToImport,
            boolean failUponDiff, boolean removeFromSourceUponSuccess, boolean setRemoveReferenceInSourceUponSuccess);
    
    void removeScope(Scope<ShardingKey> scope);
    
    /**
     * Creates a "remote server reference" on this application replica set pointing to the {@code to} replica set. If a
     * non-{@code null} sequence of {@link Scope}s is provided then the {@code includeOrExcludeScopes} flag decides
     * whether the reference shall only <em>include</em> those scopes ({@code true}) or it should list all scopes
     * <em>except those listed in {@code scopes}</em> ({@code false}) instead.
     */
    void setRemoteReference(String name, ApplicationReplicaSet<ShardingKey, MetricsT> to,
            Iterable<Scope<ShardingKey>> scopes, boolean includeOrExcludeScopes);
    
    void removeRemoteReference(String name);
    
    /**
     * Tells this replica set whether read requests may also be addressed at the master node in case there are one or
     * more {@link #getReplicas() replicas} configured. If setting this to {@code true}, the {@link #getMaster() master
     * process} will be targeted by regular read requests just like any other {@link #getReplicas() replica} will.
     * Otherwise, the master node will receive only modifying transactions, and reading requests only if no replica
     * is currently {@link Process#isAvailable() available} in this replica set.
     */
    void setReadFromMaster(boolean readFromMaster);
    
    /**
     * See {@link #setReadFromMaster(boolean)}
     */
    boolean isReadFromMaster();
    
    Map<ShardingKey, Set<ApplicationProcess<ShardingKey, MetricsT>>> getShardingInfo();
    
    /**
     * Activates sharding for the {@code shard} by configuring this replica set such that requests for the {@code shard}
     * are usually submitted to any instance from the {@code processesToPrimarilyHandleShard} set. Only if no process
     * within that set is available, the replica set will allow requests for {@code shard} to be handled by any other
     * process in this replica set as a default.
     * 
     * @param processesToPrimarilyHandleShard must not be {@code null} but can be empty
     * 
     * @see #removeSharding
     */
    void setSharding(Shard<ShardingKey> shard, Set<ApplicationProcess<ShardingKey, MetricsT>> processesToPrimarilyHandleShard);
    
    /**
     * Re-configures this replica set such that requests for {@code shard} will be spread across all processes
     * of this replica set.
     */
    void removeSharding(Shard<ShardingKey> shard);
}
