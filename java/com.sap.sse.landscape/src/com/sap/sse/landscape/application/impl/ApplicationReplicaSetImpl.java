package com.sap.sse.landscape.application.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;

public class ApplicationReplicaSetImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends NamedImpl
implements ApplicationReplicaSet<ShardingKey, MetricsT, ProcessT> {
    private static final long serialVersionUID = -4107033961273165726L;
    private ProcessT master;
    private Set<ProcessT> replicas;
    private CompletableFuture<String> hostname;
    
    /**
     * @param hostname
     *            the fully-qualified hostname under which this application replica set can be reached; this will
     *            typically be mapped to a load balancer's A-record name by using a CNAME in the DNS; usually the first
     *            part of the hostname equals the {@code replicaSetAndServerName}; if {@code null} is passed, the
     *            replica set will start the process to find out, and {@link #getHostname()} may have to block until
     *            this process has completed.
     */
    public ApplicationReplicaSetImpl(String replicaSetAndServerName, String hostname, ProcessT master, Optional<Iterable<ProcessT>> replicas) {
        super(replicaSetAndServerName);
        this.hostname = new CompletableFuture<>();
        if (hostname != null) {
            this.hostname.complete(hostname);
        }
        this.master = master;
        this.replicas = new HashSet<>();
        replicas.ifPresent(theReplicas->Util.addAll(theReplicas, this.replicas));
    }

    /**
     * Same as {@link #ApplicationReplicaSetImpl(String, String, ApplicationProcess, Optional)}, but without setting the
     * {@link #getHostname() host name} right away; instead, the host name will be discovered together with the other
     * discovery processes.
     */
    public ApplicationReplicaSetImpl(String replicaSetAndServerName, ProcessT master, Optional<Iterable<ProcessT>> replicas) {
        this(replicaSetAndServerName, /* hostname */ null, master, replicas);
    }

    @Override
    public String getHostname() throws InterruptedException, ExecutionException {
        return hostname.get();
    }
    
    protected void setHostname(String hostname) {
        this.hostname.complete(hostname);
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
}
