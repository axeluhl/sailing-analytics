package com.sap.sse.landscape.aws.orchestration;

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
HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT> {
    /**
     * When setting the {@link #setLandscape(Landscape) landscape} or the {@link #setRegion(AwsRegion) region}, these
     * settings are also copied to the {@link AwsApplicationConfiguration.Builder application configuration builder}.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartAwsHost.Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT> {
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>,
    T extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartAwsHost.BuilderImpl<BuilderT, T, ShardingKey, MetricsT, ProcessT, HostT>
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
        public BuilderT setLandscape(Landscape<ShardingKey, MetricsT, ProcessT> landscape) {
            getApplicationConfigurationBuilder().setLandscape((AwsLandscape<ShardingKey, MetricsT, ProcessT>) landscape);
            return super.setLandscape(landscape);
        }
    }
    
    private final AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> applicationConfiguration;
    
    protected StartAwsApplicationHost(BuilderImpl<?, ? extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT> builder) throws Exception {
        super(builder);
        applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
        addUserData(applicationConfiguration::getUserData);
    }

    protected AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT> getApplicationConfiguration() {
        return applicationConfiguration;
    }
}
