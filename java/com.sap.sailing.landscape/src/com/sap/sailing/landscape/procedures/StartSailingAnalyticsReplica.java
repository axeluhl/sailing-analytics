package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.impl.SailingAnalyticsReplicaImpl;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.aws.orchestration.OutboundReplicationConfiguration;

public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsReplica<ShardingKey>> {
    public static interface Builder<ShardingKey>
    extends StartSailingAnalyticsHost.Builder<StartSailingAnalyticsReplica<ShardingKey>, ShardingKey, SailingAnalyticsReplica<ShardingKey>> {
    }
    
    protected static class BuilderImpl<ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<StartSailingAnalyticsReplica<ShardingKey>, ShardingKey, SailingAnalyticsReplica<ShardingKey>>
    implements Builder<ShardingKey> {
        private static final String DEFAULT_REPLICA_INSTANCE_NAME_SUFFIX = " (Replica)";
        private static final String DEFAULT_REPLICA_OUTPUT_REPLICATION_EXCHANGE_NAME_SUFFIX = "-replica";

        @Override
        public StartSailingAnalyticsReplica<ShardingKey> build() {
            return new StartSailingAnalyticsReplica<ShardingKey>(this);
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
                resultBuilder.setOutboundReplicationExchangeName(super.getOutboundReplicationConfiguration() + DEFAULT_REPLICA_OUTPUT_REPLICATION_EXCHANGE_NAME_SUFFIX);
            }
            return resultBuilder.build();
        }
    }
    
    public static <ShardingKey> Builder<ShardingKey> builder() {
        return new BuilderImpl<>();
    }
    
    protected StartSailingAnalyticsReplica(Builder<ShardingKey> builder) {
        super(builder);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-replica-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }

    @Override
    public SailingAnalyticsReplica<ShardingKey> getSailingAnalyticsProcess() {
        return new SailingAnalyticsReplicaImpl<>(getPort(), getHost(), getServerDirectory());
    }
}
