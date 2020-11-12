package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.ApplicationProcessHost;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.aws.impl.ApacheReverseProxy;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;

public class ApplicationProcessHostImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends AwsInstanceImpl<ShardingKey, MetricsT>
implements ApplicationProcessHost<ShardingKey, MetricsT> {
    public ApplicationProcessHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            AwsLandscape<ShardingKey, MetricsT, ?, ?> landscape) {
        super(instanceId, availabilityZone, landscape);
    }
    
    @Override
    protected AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        @SuppressWarnings("unchecked")
        final AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> castLandscape =
            (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
        return castLandscape;
    }

    @Override
    public ReverseProxy<ShardingKey, MetricsT, RotatingFileBasedLog> getReverseProxy() {
        return new ApacheReverseProxy<>(getLandscape(), this);
    }

    /**
     * The implementation scans the {@link ApplicationProcessHost#DEFAULT_SERVERS_PATH application server deployment
     * folder} for sub-folders. In those sub-folders, the configuration file is analyzed
     */
    @Override
    public Iterable<ApplicationProcess<ShardingKey, MetricsT>> getApplicationProcesses() {
        // TODO Implement SailingAnalyticsHost<ShardingKey>.getSailingAnalyticsProcesses(...)
        return null;
    }
}
