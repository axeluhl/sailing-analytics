package com.sap.sailing.landscape.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.shared.server.SharedSailingData;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.aws.AwsLandscapeState;
import com.sap.sse.security.SecurityService;

public class SailingAnalyticsMasterConfiguration<ShardingKey>
extends SailingAnalyticsApplicationConfiguration<ShardingKey> {
    /**
     * The {@link DefaultProcessConfigurationVariables#USE_ENVIRONMENT} variable is set to {@code "live-master-server"}
     * by this builder.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.Builder<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey>, ShardingKey> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends SailingAnalyticsApplicationConfiguration.BuilderImpl<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        /**
         * The security-service application replica set currently has a deviating, non-default RabbitMQ exchange name
         * used for its replication. By default, exchanges are named after the replica set name, but here the legacy
         * name has an underscore instead of a dash.
         */
        private static final String SECURITY_SERVICE_EXCHANGE_NAME = "security_service";

        @Override
        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> result = new HashMap<>(super.getUserData());
            result.put(DefaultProcessConfigurationVariables.REPLICATE_ON_START,
                    Util.join(",", SecurityService.REPLICABLE_FULLY_QUALIFIED_CLASSNAME,
                                   SharedSailingData.REPLICABLE_FULLY_QUALIFIED_CLASSNAME,
                                   AwsLandscapeState.REPLICABLE_FULLY_QUALIFIED_CLASSNAME));
            result.put(DefaultProcessConfigurationVariables.REPLICATE_MASTER_SERVLET_HOST,
                    getDefaultSecurityServiceReplicaSetHostname(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME,
                                                                SharedLandscapeConstants.DEFAULT_SECURITY_SERVICE_REPLICA_SET_NAME));
            result.put(DefaultProcessConfigurationVariables.REPLICATE_MASTER_EXCHANGE_NAME, SECURITY_SERVICE_EXCHANGE_NAME);
            return result;
        }

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
