package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.aws.ApplicationProcessHost;

/**
 * This procedure does two things: it {@link StartSailingAnalyticsHost starts} a {@link ApplicationProcessHost}, and
 * (currently implicitly, based on the way the /etc/init.d/sailing script works) also starts a "master" process that is
 * expected to have the default working directory {@code /home/sailing/servers/server} (see
 * {@link ApplicationProcessHost#DEFAULT_SERVER_PATH}). The configuration for the master process mainly comes from a
 * {@link SailingAnalyticsApplicationConfiguration.Builder} that must be passed to the {@link Builder} for object of
 * this type. When finally constructing the object of this type, the application configuration builder is used to
 * produce a {@link SailingAnalyticsApplicationConfiguration} object whose
 * {@link SailingAnalyticsApplicationConfiguration#getUserData() user data} are then used for launching the instance,
 * enriched by the {@code live-master-server} value for the {@link DefaultProcessConfigurationVariables#USE_ENVIRONMENT
 * USE_ENVIRONMENT} variable.
 * <p>
 *
 * @author Axel Uhl (D043530)
 */
public class StartSailingAnalyticsMasterHost<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey> {
    private static final String DEFAULT_MASTER_INSTANCE_NAME_SUFFIX = " (Master)";
    
    // TODO the default for the inbound replication configuration should be based on security-service.sapsailing.com and SecurityService/SharedSailingData, making live-master-server environment irrelevant
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.Builder<BuilderT, StartSailingAnalyticsMasterHost<ShardingKey>, ShardingKey> {
    }
    
    // TODO model an AwsLandscape subclass describing the specifics of the Sailing landscape, with a central security service that a master replicates by default
    // TODO or is this a property for this procedure (with/without central security/shared sailing data) which then knows about security-service.sapsailing.com?
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<BuilderT, StartSailingAnalyticsMasterHost<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        protected BuilderImpl(SailingAnalyticsMasterConfiguration.Builder<?, ShardingKey> applicationConfigurationBuilder) {
            super(applicationConfigurationBuilder);
        }

        @Override
        public StartSailingAnalyticsMasterHost<ShardingKey> build() throws Exception {
            return new StartSailingAnalyticsMasterHost<>(this);
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : super.getInstanceName() + DEFAULT_MASTER_INSTANCE_NAME_SUFFIX;
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> BuilderT masterHostBuilder(
            SailingAnalyticsMasterConfiguration.Builder<?, ShardingKey> applicationConfigurationBuilder) {
        @SuppressWarnings("unchecked")
        final BuilderT result = (BuilderT) new BuilderImpl<BuilderT, ShardingKey>(applicationConfigurationBuilder);
        return result;
    }

    protected StartSailingAnalyticsMasterHost(BuilderImpl<?, ShardingKey> builder) throws Exception {
        super(builder);
    }
}
