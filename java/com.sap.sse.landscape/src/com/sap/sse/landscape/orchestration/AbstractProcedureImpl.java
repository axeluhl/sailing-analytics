package com.sap.sse.landscape.orchestration;

import java.util.Optional;

import com.sap.sse.common.Duration;
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
        private Optional<Duration> optionalTimeout;

        protected Landscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
            return landscape;
        }

        @Override
        public BuilderT setLandscape(Landscape<ShardingKey, MetricsT, ProcessT> landscape) {
            this.landscape = landscape;
            return self();
        }
        
        /**
         * A timeout for interacting with the instance, such as when creating an SSH / SFTP connection or waiting for its
         * public IP address.
         */
        protected Optional<Duration> getOptionalTimeout() {
            return optionalTimeout == null ? Optional.empty() : optionalTimeout;
        }

        @Override
        public BuilderT setOptionalTimeout(
                Optional<Duration> optionalTimeout) {
            this.optionalTimeout = optionalTimeout;
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
