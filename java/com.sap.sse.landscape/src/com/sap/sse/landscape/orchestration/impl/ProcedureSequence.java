package com.sap.sse.landscape.orchestration.impl;

import java.util.Iterator;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

public class ProcedureSequence<ShardingKey, 
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AbstractProcedureImpl<ShardingKey,MetricsT,ProcessT> 
implements Procedure<ShardingKey, MetricsT, ProcessT>, Iterable<Procedure<ShardingKey, MetricsT, ProcessT>> {
    private final Iterable<Procedure<ShardingKey, MetricsT, ProcessT>> steps;
    
    public ProcedureSequence(Landscape<ShardingKey, MetricsT, ProcessT> landscape,
            Iterable<Procedure<ShardingKey, MetricsT, ProcessT>> steps) {
        super(landscape);
        this.steps = steps;
    }

    @Override
    public void run() throws Exception {
        for (final Procedure<ShardingKey, MetricsT, ProcessT> s : this) {
            s.run();
        }
    }

    @Override
    public Iterator<Procedure<ShardingKey, MetricsT, ProcessT>> iterator() {
        return steps.iterator();
    }
}
