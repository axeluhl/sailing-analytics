package com.sap.sailing.landscape.procedures;

import java.util.Optional;

import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;

public class StartSailingAnalyticsReplica<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey> {
    /**
     * Additional defaults for starting a Sailing Analytics replica server:
     * <ul>
     * <li>The {@link #getOutboundReplicationConfiguration() output replication}
     * {@link OutboundReplicationConfiguration#getOutboundReplicationExchangeName() exchange name} defaults to the
     * {@link #getServerName() server name} with the suffix {@code -replica} appended to it.</li>
     * <li>The instance name defaults to the superclass's default ("SL {servername}") with the string
     * {@code " (Replica)"} appended to it.</li>
     * <li>The {@link #getInboundReplicationConfiguration() inbound replication}
     * {@link InboundReplicationConfiguration#getInboundMasterExchangeName() exchange name} defaults to the
     * {@link #setServerName(String) server name} property</li>
     * </ul>
     * 
     * TODO we could default the set of replicables to replicate, competing with the live-replica-server environment contents
     * 
     * @author Axel Uhl (D043530)
     *
     * @param <BuilderT>
     * @param <ShardingKey>
     */
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

        @Override
        public Optional<InboundReplicationConfiguration> getInboundReplicationConfiguration() {
            final InboundReplicationConfiguration.Builder resultBuilder;
            if (super.getInboundReplicationConfiguration() != null && super.getInboundReplicationConfiguration().isPresent()) {
                resultBuilder = InboundReplicationConfiguration.copy(super.getInboundReplicationConfiguration().get());
            } else {
                resultBuilder = InboundReplicationConfiguration.builder();
            }
            if (!isInboundReplicationExchangeNameSet()) {
                resultBuilder.setInboundMasterExchangeName(getServerName());
            }
            return Optional.of(resultBuilder.build());
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
