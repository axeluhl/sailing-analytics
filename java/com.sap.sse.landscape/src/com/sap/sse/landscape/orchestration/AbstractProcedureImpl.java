package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public abstract class AbstractProcedureImpl<ShardingKey, 
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>> implements Procedure<ShardingKey, MetricsT, ProcessT> {
    private final Landscape<ShardingKey, MetricsT, ProcessT> landscape;

    public AbstractProcedureImpl(Landscape<ShardingKey, MetricsT, ProcessT> landscape) {
        super();
        this.landscape = landscape;
    }

    @Override
    public Landscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return landscape;
    }

}
