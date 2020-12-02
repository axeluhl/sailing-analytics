package com.sap.sailing.landscape.procedures;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;

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
        @Override
        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> result = new HashMap<>(super.getUserData());
            result.put(DefaultProcessConfigurationVariables.USE_ENVIRONMENT, "live-master-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
            return result;
        }

        @Override
        public SailingAnalyticsMasterConfiguration<ShardingKey> build() throws Exception {
            return new SailingAnalyticsMasterConfiguration<ShardingKey>(this);
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> Builder<BuilderT, ShardingKey> builder() {
        return new BuilderImpl<>();
    }

    protected SailingAnalyticsMasterConfiguration(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
    }
}
