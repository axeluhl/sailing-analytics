package com.sap.sse.landscape.orchestration.impl;

import java.util.Iterator;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public class ProcedureSequence<ShardingKey, 
MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends AbstractProcedureImpl<ShardingKey,MetricsT,MasterProcessT,ReplicaProcessT> 
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, Iterable<Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> {
    private final Iterable<Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> steps;
    
    public ProcedureSequence(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape,
            Iterable<Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> steps) {
        super(landscape);
        this.steps = steps;
    }

    @Override
    public void run() throws Exception {
        for (final Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> s : this) {
            s.run();
        }
    }

    @Override
    public Iterator<Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> iterator() {
        return steps.iterator();
    }
}
