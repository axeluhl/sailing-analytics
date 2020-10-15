package com.sap.sse.landscape.aws.impl;

import java.util.UUID;

import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;

/**
 * An Apache2-based reverse proxy implementation (httpd) that makes specific assumptions about the availability of an
 * {@link AmazonMachineImage} that can be used to launch and configure such a reverse proxy instance on one or more
 * instances running in one or more {@link AwsAvailabilityZone availability zones}.<p>
 * 
 * TODO how do we remember the hosts/instances/nodes/processes that together form this {@link ApacheReverseProxy}? DB Persistence? Tags?
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
LogT extends Log>
extends AbstractApacheReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, LogT> {
    private static final long serialVersionUID = 8019146973512856147L;
    private AwsInstance<ShardingKey, MetricsT> host;
    
    public ApacheReverseProxy(String name, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, AwsRegion region) {
        super(name, landscape, region);
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ApacheReverseProxy.setScopeRedirect(...)
        
    }

    @Override
    public void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setPlainRedirect(...)
        
    }

    @Override
    public void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setHomeRedirect(...)
        
    }

    @Override
    public void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet, UUID eventId) {
        // TODO Implement ReverseProxy.setEventRedirect(...)
        
    }

    @Override
    public void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet,
            UUID leaderboardGroupId) {
        // TODO Implement ReverseProxy.setEventSeriesRedirect(...)
        
    }

    @Override
    public void removeRedirect(String hostname) {
        // TODO Implement ReverseProxy.removeRedirect(...)
        
    }

    @Override
    public void terminate() {
        getLandscape().terminate(host);
    }

    @Override
    public int getPort() {
        // TODO Implement Process<LogT,MetricsT>.getPort(...)
        return 0;
    }

    @Override
    public AwsInstance<ShardingKey, MetricsT> getHost() {
        return host;
    }

    @Override
    public LogT getLog() {
        // TODO Implement Process<LogT,MetricsT>.getLog(...)
        return null;
    }

    @Override
    public MetricsT getMetrics() {
        // TODO Implement Process<LogT,MetricsT>.getMetrics(...)
        return null;
    }

    @Override
    public boolean isAlive() {
        // TODO Implement Process<LogT,MetricsT>.isAlive(...)
        return false;
    }

    @Override
    public boolean isReady() {
        // TODO Implement Process<LogT,MetricsT>.isReady(...)
        return false;
    }
}