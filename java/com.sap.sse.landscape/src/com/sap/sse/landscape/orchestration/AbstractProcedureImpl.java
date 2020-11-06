package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public abstract class AbstractProcedureImpl<ShardingKey, 
MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private final Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;

    public AbstractProcedureImpl(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super();
        this.landscape = landscape;
    }

    @Override
    public Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return landscape;
    }

}
