package com.sap.sse.landscape.aws.orchestration;

import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;

/**
 * In addition to launching a host, this procedure launches an initial application server process on that host.
 * Therefore, it offers the configuration of release, server name, as well as replication properties for the application
 * server process through a dedicated {@link AwsApplicationConfiguration.Builder application configuration builder}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StartAwsApplicationHost<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
HostT extends AwsInstance<ShardingKey>>
extends StartAwsHost<ShardingKey, HostT> {
    /**
     * When setting the {@link #setLandscape(Landscape) landscape} or the {@link #setRegion(AwsRegion) region}, these
     * settings are also copied to the {@link AwsApplicationConfiguration.Builder application configuration builder}.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartAwsHost<ShardingKey, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey>>
    extends StartAwsHost.Builder<BuilderT, T, ShardingKey, HostT> {
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartAwsHost<ShardingKey, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey>>
    extends StartAwsHost.BuilderImpl<BuilderT, T, ShardingKey, HostT>
    implements Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
        private final AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT> applicationConfigurationBuilder;

        protected BuilderImpl(AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT> applicationConfigurationBuilder) {
            super();
            this.applicationConfigurationBuilder = applicationConfigurationBuilder;
        }
        
        protected AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT> getApplicationConfigurationBuilder() {
            return applicationConfigurationBuilder;
        }

        @Override
        public BuilderT setRegion(AwsRegion region) {
            getApplicationConfigurationBuilder().setRegion(region);
            return super.setRegion(region);
        }

        @Override
        public BuilderT setLandscape(Landscape<ShardingKey> landscape) {
            getApplicationConfigurationBuilder().setLandscape((AwsLandscape<ShardingKey>) landscape);
            return super.setLandscape(landscape);
        }

        /**
         * make visible in package
         */
        @Override
        protected Optional<Duration> getOptionalTimeout() {
            return super.getOptionalTimeout();
        }
    }
    
    private final AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration;
    private final Optional<Duration> optionalTimeout;
    
    protected StartAwsApplicationHost(BuilderImpl<?, ? extends StartAwsHost<ShardingKey, HostT>, ShardingKey, MetricsT, ProcessT, HostT> builder) throws Exception {
        super(builder);
        this.optionalTimeout = builder.getOptionalTimeout();
        applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
        addUserData(applicationConfiguration::getUserData);
    }

    protected AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> getApplicationConfiguration() {
        return applicationConfiguration;
    }
    
    protected Optional<Duration> getOptionalTimeout() {
        return optionalTimeout;
    }
}
