package com.sap.sse.landscape.application.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.application.Shard;

public class ApplicationReplicaSetImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends NamedImpl
implements ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> {
    private static final long serialVersionUID = -4107033961273165726L;
    private ProcessT master;
    private Set<ProcessT> replicas;
    private final String hostname;
    
    /**
     * @param hostname
     *            the fully-qualified hostname under which this application replica set can be reached; this will
     *            typically be mapped to a load balancer's A-record name by using a CNAME in the DNS; usually the first
     *            part of the hostname equals the {@code replicaSetAndServerName}
     */
    public ApplicationReplicaSetImpl(String replicaSetAndServerName, String hostname, ProcessT master, Optional<Iterable<ProcessT>> replicas) {
        super(replicaSetAndServerName);
        this.hostname = hostname;
        this.master = master;
        this.replicas = new HashSet<>();
        replicas.ifPresent(theReplicas->Util.addAll(theReplicas, this.replicas));
    }
    
    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public void upgrade(Release newVersion) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.upgrade(...)
    }

    @Override
    public ProcessT getMaster() {
        return master;
    }

    @Override
    public Iterable<ProcessT> getReplicas() {
        return Collections.unmodifiableSet(replicas);
    }

    @Override
    public Iterable<Scope<ShardingKey>> getScopes() {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.getScopes(...)
        return null;
    }

    @Override
    public void importScope(ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> source,
            Scope<ShardingKey> scopeToImport, boolean failUponDiff, boolean removeFromSourceUponSuccess,
            boolean setRemoveReferenceInSourceUponSuccess) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.importScope(...)
    }

    @Override
    public void removeScope(Scope<ShardingKey> scope) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.removeScope(...)
        
    }

    @Override
    public void setRemoteReference(String name, ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> to,
            Iterable<Scope<ShardingKey>> scopes, boolean includeOrExcludeScopes) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.setRemoteReference(...)
        // use /v1/remoteserverreference (RemoteServerReferenceResource) for this
    }

    @Override
    public void removeRemoteReference(String name) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.removeRemoteReference(...)
        // use /v1/remoteserverreference (RemoteServerReferenceResource) for this
    }

    @Override
    public void setReadFromMaster(boolean readFromMaster) throws IllegalStateException {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.setReadFromMaster(...)
        // for this it would be helpful to understand the ALB / TargetGroup assignments
    }

    @Override
    public boolean isReadFromMaster() {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.isReadFromMaster(...)
        // for this it would be helpful to understand the ALB / TargetGroup assignments
        return false;
    }

    @Override
    public Map<ShardingKey, Set<ApplicationProcess<ShardingKey, MetricsT, ProcessT>>> getShardingInfo() {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.getShardingInfo(...)
        return null;
    }

    @Override
    public void setSharding(Shard<ShardingKey> shard,
            Set<ApplicationProcess<ShardingKey, MetricsT, ProcessT>> processesToPrimarilyHandleShard) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.setSharding(...)
        
    }

    @Override
    public void removeSharding(Shard<ShardingKey> shard) {
        // TODO Implement ApplicationReplicaSet<ShardingKey,MetricsT,ProcessT>.removeSharding(...)
        
    }
}
