package com.sap.sse.landscape.aws.impl;

import java.util.UUID;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
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
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends AbstractApacheReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
implements com.sap.sse.landscape.Process<RotatingFileBasedLog, MetricsT> {
    private AwsInstance<ShardingKey, MetricsT> host;
    
    public ApacheReverseProxy(AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, AwsInstance<ShardingKey, MetricsT> host) {
        super(landscape);
        this.host = host;
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
    public boolean isReady() {
        // TODO Implement Process<LogT,MetricsT>.isReady(...)
        return false;
    }
}