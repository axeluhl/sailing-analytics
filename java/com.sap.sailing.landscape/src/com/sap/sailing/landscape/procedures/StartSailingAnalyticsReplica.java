package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;

public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey> {
    // TODO the inbound replication configuration should be based on the SERVER_NAME property
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.Builder<BuilderT, StartSailingAnalyticsReplica<ShardingKey>, ShardingKey> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<BuilderT, StartSailingAnalyticsReplica<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        private static final String DEFAULT_REPLICA_INSTANCE_NAME_SUFFIX = " (Replica)";
        private static final String DEFAULT_REPLICA_OUTPUT_REPLICATION_EXCHANGE_NAME_SUFFIX = "-replica";

        @Override
        public StartSailingAnalyticsReplica<ShardingKey> build() {
            return new StartSailingAnalyticsReplica<>(this);
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : super.getInstanceName() + DEFAULT_REPLICA_INSTANCE_NAME_SUFFIX;
        }

        @Override
        public OutboundReplicationConfiguration getOutboundReplicationConfiguration() {
            final OutboundReplicationConfiguration.Builder resultBuilder;
            if (super.getOutboundReplicationConfiguration() != null) {
                resultBuilder = OutboundReplicationConfiguration.copy(super.getOutboundReplicationConfiguration());
            } else {
                resultBuilder = OutboundReplicationConfiguration.builder();
            }
            if (!isOutboundReplicationExchangeNameSet()) {
                // We assume here that the superclass implementation will default the exchange name to the server name
                resultBuilder.setOutboundReplicationExchangeName(
                        super.getOutboundReplicationConfiguration().getOutboundReplicationExchangeName() +
                        DEFAULT_REPLICA_OUTPUT_REPLICATION_EXCHANGE_NAME_SUFFIX);
            }
            return resultBuilder.build();
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> Builder<BuilderT, ShardingKey> builder() {
        return new BuilderImpl<>();
    }
    
    protected StartSailingAnalyticsReplica(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-replica-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }
}
