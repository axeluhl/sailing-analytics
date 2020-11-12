package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;

public abstract class AbstractApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
implements ReverseProxy<ShardingKey, MetricsT, RotatingFileBasedLog> {
    private final AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
    
    public AbstractApacheReverseProxy(AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        this.landscape = landscape;
    }

    protected AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return landscape;
    }

    @Override
    public String getHealthCheckPath() {
        return "/internal-server-status";
    }
}
