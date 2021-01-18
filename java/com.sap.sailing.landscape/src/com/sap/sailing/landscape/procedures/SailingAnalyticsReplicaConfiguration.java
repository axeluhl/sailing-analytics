package com.sap.sailing.landscape.procedures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.OutboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.aws.AwsInstance;

public class SailingAnalyticsReplicaConfiguration<ShardingKey>
extends SailingAnalyticsApplicationConfiguration<ShardingKey> {
    /**
     * Additional defaults for starting a Sailing Analytics replica server:
     * <ul>
     * <li>The {@link #getOutboundReplicationConfiguration() output replication}
     * {@link OutboundReplicationConfiguration#getOutboundReplicationExchangeName() exchange name} defaults to the
     * {@link #getServerName() server name} with the suffix {@code -replica} appended to it.</li>
     * <li>The {@link #getInboundReplicationConfiguration() inbound replication}
     * {@link InboundReplicationConfiguration#getInboundMasterExchangeName() exchange name} defaults to the
     * {@link #setServerName(String) server name} property</li>
     * <li>The {@link DefaultProcessConfigurationVariables#USE_ENVIRONMENT} variable is set to {@code "live-replica-server"}.</li>
     * </ul>
     * 
     * TODO we could default the set of replicables to replicate, competing with the live-replica-server environment contents
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.Builder<BuilderT, SailingAnalyticsReplicaConfiguration<ShardingKey>, ShardingKey> {
        String DEFAULT_REPLICA_OUTPUT_REPLICATION_EXCHANGE_NAME_SUFFIX = "-replica";
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.BuilderImpl<BuilderT, SailingAnalyticsReplicaConfiguration<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
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
        
        @Override
        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> result = new HashMap<>(super.getUserData());
            result.put(DefaultProcessConfigurationVariables.USE_ENVIRONMENT, "live-replica-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
            return result;
        }

        @Override
        public SailingAnalyticsReplicaConfiguration<ShardingKey> build() throws Exception {
            return new SailingAnalyticsReplicaConfiguration<ShardingKey>(this);
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>,
    ShardingKey extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> BuilderT replicaBuilder() {
        @SuppressWarnings("unchecked")
        final BuilderT result = (BuilderT) new BuilderImpl<BuilderT, ShardingKey>();
        return result;
    }

    protected SailingAnalyticsReplicaConfiguration(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
    }
}
