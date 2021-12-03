package com.sap.sse.landscape.orchestration;

import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Landscape;

public abstract class AbstractProcedureImpl<ShardingKey>
implements Procedure<ShardingKey> {
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends Procedure<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, T, ShardingKey> {
        private Landscape<ShardingKey> landscape;
        private Optional<Duration> optionalTimeout;

        protected Landscape<ShardingKey> getLandscape() {
            return landscape;
        }

        @Override
        public BuilderT setLandscape(Landscape<ShardingKey> landscape) {
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
    
    private final Landscape<ShardingKey> landscape;

    protected AbstractProcedureImpl(BuilderImpl<?, ?, ShardingKey> builder) {
        this.landscape = builder.getLandscape();
    }

    @Override
    public Landscape<ShardingKey> getLandscape() {
        return landscape;
    }
}
