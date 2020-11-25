package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;

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

        protected BuilderImpl(SailingAnalyticsApplicationConfiguration.Builder<?, ?, ShardingKey> applicationConfigurationBuilder) {
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
        addUserData(DefaultProcessConfigurationVariables.USE_ENVIRONMENT, "live-replica-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }
}
