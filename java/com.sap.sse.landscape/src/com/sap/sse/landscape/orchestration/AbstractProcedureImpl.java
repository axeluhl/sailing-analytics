package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public abstract class AbstractProcedureImpl<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
implements Procedure<ShardingKey, MetricsT, ProcessT> {
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT>,
    T extends Procedure<ShardingKey, MetricsT, ProcessT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT> {
        private Landscape<ShardingKey, MetricsT, ProcessT> landscape;

        protected Landscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
            return landscape;
        }

        @Override
        public BuilderT setLandscape(Landscape<ShardingKey, MetricsT, ProcessT> landscape) {
            this.landscape = landscape;
            return self();
        }
    }
    
    private final Landscape<ShardingKey, MetricsT, ProcessT> landscape;

    protected AbstractProcedureImpl(BuilderImpl<?, ?, ShardingKey, MetricsT, ProcessT> builder) {
        this.landscape = builder.getLandscape();
    }

    @Override
    public Landscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return landscape;
    }
}
