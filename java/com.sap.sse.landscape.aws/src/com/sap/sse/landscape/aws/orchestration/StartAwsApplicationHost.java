package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsInstance;

/**
 * In addition to launching a host, this procedure launches an initial application server process on that host.
 * Therefore, it offers the configuration of release, server name, as well as replication properties for the application
 * server process.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StartAwsApplicationHost<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT> {
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
        private final AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT, HostT> applicationConfigurationBuilder;

        protected BuilderImpl(AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT, HostT> applicationConfigurationBuilder) {
            super();
            this.applicationConfigurationBuilder = applicationConfigurationBuilder;
        }
        
        protected AwsApplicationConfiguration.Builder<?, ?, ShardingKey, MetricsT, ProcessT, HostT> getApplicationConfigurationBuilder() {
            return applicationConfigurationBuilder;
        }
    }
    
    private final AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT, HostT> applicationConfiguration;
    
    protected StartAwsApplicationHost(BuilderImpl<?, ? extends StartAwsHost<ShardingKey, MetricsT, ProcessT, HostT>, ShardingKey, MetricsT, ProcessT, HostT> builder) throws Exception {
        super(builder);
        applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
        addUserData(applicationConfiguration::getUserData);
    }

    protected AwsApplicationConfiguration<ShardingKey, MetricsT, ProcessT, HostT> getApplicationConfiguration() {
        return applicationConfiguration;
    }
}
