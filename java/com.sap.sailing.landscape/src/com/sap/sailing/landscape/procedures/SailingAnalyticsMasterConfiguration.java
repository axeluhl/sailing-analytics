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
            // TODO bug5684: maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing? It would allow us to keep the knowledge about which SecurityService is being replicated by a master by default in one place.
            result.put(DefaultProcessConfigurationVariables.USE_ENVIRONMENT, "live-master-server");
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
