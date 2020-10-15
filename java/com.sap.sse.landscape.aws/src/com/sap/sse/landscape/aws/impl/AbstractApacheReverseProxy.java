package com.sap.sse.landscape.aws.impl;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;

public abstract class AbstractApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
LogT extends Log>
extends NamedImpl implements ReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, LogT> {
    private static final long serialVersionUID = 8019146973512856147L;
    private final AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
    
    public AbstractApacheReverseProxy(String name, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, AwsRegion region) {
        super(name);
        this.landscape = landscape;
    }

    protected AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return landscape;
    }

    @Override
    public String getHealthCheckPath() {
        return "/internal-server-status";
    }

    @Override
    public String getTargetGroupName() {
        return "ReverseProxy-"+getName();
    }
}
