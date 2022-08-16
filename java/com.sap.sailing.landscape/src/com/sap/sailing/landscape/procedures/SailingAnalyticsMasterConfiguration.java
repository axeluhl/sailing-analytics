package com.sap.sailing.landscape.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.shared.server.SharedSailingData;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.aws.AwsLandscapeState;
import com.sap.sse.security.SecurityService;

public class SailingAnalyticsMasterConfiguration<ShardingKey>
extends SailingAnalyticsApplicationConfiguration<ShardingKey> {
    /**
     * If not set, the {@link DefaultProcessConfigurationVariables#REPLICATE_MASTER_EXCHANGE_NAME} variable will default
     * to the value of {@link Builder#SECURITY_SERVICE_EXCHANGE_NAME}. Furthermore, if not otherwise specified, the
     * {@link DefaultProcessConfigurationVariables#REPLICATE_MASTER_SERVLET_HOST} will default to the central security
     * service replica set's hostname as obtained through
     * {@link SharedLandscapeConstants#DEFAULT_SECURITY_SERVICE_REPLICA_SET_NAME} and
     * {@link SharedLandscapeConstants#DEFAULT_DOMAIN_NAME}.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.Builder<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey>, ShardingKey> {
        /**
         * The security-service application replica set currently has a deviating, non-default RabbitMQ exchange name
         * used for its replication. By default, exchanges are named after the replica set name, but here the legacy
         * name has an underscore instead of a dash.
         */
        String SECURITY_SERVICE_EXCHANGE_NAME = "security_service";
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.BuilderImpl<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {

        @Override
        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> result = new HashMap<>(super.getUserData());
            result.put(DefaultProcessConfigurationVariables.REPLICATE_ON_START,
                    Util.join(",", SecurityService.REPLICABLE_FULLY_QUALIFIED_CLASSNAME,
                                   SharedSailingData.REPLICABLE_FULLY_QUALIFIED_CLASSNAME,
                                   AwsLandscapeState.REPLICABLE_FULLY_QUALIFIED_CLASSNAME));
            return result;
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
                resultBuilder.setInboundMasterExchangeName(SECURITY_SERVICE_EXCHANGE_NAME);
            }
            if (!isInboundMasterServletHostSet()) {
                resultBuilder.setMasterHostname(getDefaultSecurityServiceReplicaSetHostname(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME,
                                                                                            SharedLandscapeConstants.DEFAULT_SECURITY_SERVICE_REPLICA_SET_NAME));
            }
            return Optional.of(resultBuilder.build());
        }
        
        /**
         * An application replica set's master process will fetch polar data and wind estimation data from the
         * archive reachable at {@link SharedLandscapeConstants#DEFAULT_DOMAIN_NAME}.
         */
        @Override
        protected Iterable<String> getAdditionalJavaArgs() {
            final List<String> result = new ArrayList<>();
            Util.addAll(super.getAdditionalJavaArgs(), result);
            result.add(getAdditionalJavaArgForWindEstimation(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME));
            result.add(getAdditionalJavaArgForPolarData(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME));
            return result;
        }

        @Override
        public SailingAnalyticsMasterConfiguration<ShardingKey> build() throws Exception {
            return new SailingAnalyticsMasterConfiguration<ShardingKey>(this);
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> BuilderT masterBuilder() {
        @SuppressWarnings("unchecked")
        final BuilderT result = (BuilderT) new BuilderImpl<>();
        return result;
    }

    protected SailingAnalyticsMasterConfiguration(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
    }
}
