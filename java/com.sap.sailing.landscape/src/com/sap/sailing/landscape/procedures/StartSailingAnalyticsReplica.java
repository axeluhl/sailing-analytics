package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sse.landscape.ProcessConfigurationVariable;

public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public static interface Builder<ShardingKey>
    extends StartSailingAnalyticsHost.Builder<StartSailingAnalyticsReplica<ShardingKey>, ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    }
    
    protected static class BuilderImpl<ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<StartSailingAnalyticsReplica<ShardingKey>, ShardingKey, SailingAnalyticsHost<ShardingKey>>
    implements Builder<ShardingKey> {
        @Override
        public StartSailingAnalyticsReplica<ShardingKey> build() {
            return new StartSailingAnalyticsReplica<ShardingKey>(this);
        }
    }
    
    public static <ShardingKey> Builder<ShardingKey> builder() {
        return new BuilderImpl<>();
    }
    
    protected StartSailingAnalyticsReplica(Builder<ShardingKey> builder) {
        super(builder);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-replica-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }
}
