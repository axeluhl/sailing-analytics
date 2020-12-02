package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.aws.ApplicationProcessHost;

/**
 * This procedure does two things: it {@link StartSailingAnalyticsHost starts} an {@link ApplicationProcessHost}, and
 * (currently implicitly, based on the way the /etc/init.d/sailing script works) also starts a "replica" process that is
 * expected to have the default working directory {@code /home/sailing/servers/server} (see
 * {@link ApplicationProcessHost#DEFAULT_SERVER_PATH}). The configuration for the replica process mainly comes from a
 * {@link SailingAnalyticsApplicationConfiguration.Builder} that must be passed to the {@link Builder} for object of
 * this type. When finally constructing the object of this type, the application configuration builder is used to
 * produce a {@link SailingAnalyticsApplicationConfiguration} object whose
 * {@link SailingAnalyticsApplicationConfiguration#getUserData() user data} are then used for launching the instance,
 * enriched by the {@code live-replica-server} value for the {@link DefaultProcessConfigurationVariables#USE_ENVIRONMENT
 * USE_ENVIRONMENT} variable.
 * <p>
 *
 * @author Axel Uhl (D043530)
 */
public class StartSailingAnalyticsReplicaHost<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey> {
    /**
     * Additional defaults for starting a Sailing Analytics replica server:
     * <ul>
     * <li>The instance name defaults to the superclass's default ("SL {servername}") with the string
     * {@code " (Replica)"} appended to it.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.Builder<BuilderT, StartSailingAnalyticsReplicaHost<ShardingKey>, ShardingKey> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<BuilderT, StartSailingAnalyticsReplicaHost<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        private static final String DEFAULT_REPLICA_INSTANCE_NAME_SUFFIX = " (Replica)";

        protected BuilderImpl(SailingAnalyticsReplicaConfiguration.Builder<?, ShardingKey> applicationConfigurationBuilder) {
            super(applicationConfigurationBuilder);
        }
        
        @Override
        public StartSailingAnalyticsReplicaHost<ShardingKey> build() throws Exception {
            return new StartSailingAnalyticsReplicaHost<>(this);
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : super.getInstanceName() + DEFAULT_REPLICA_INSTANCE_NAME_SUFFIX;
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> Builder<BuilderT, ShardingKey> builder(
            SailingAnalyticsReplicaConfiguration.Builder<?, ShardingKey> applicationConfigurationBuilder) {
        return new BuilderImpl<>(applicationConfigurationBuilder);
    }
    
    protected StartSailingAnalyticsReplicaHost(BuilderImpl<?, ShardingKey> builder) throws Exception {
        super(builder);
    }
}
