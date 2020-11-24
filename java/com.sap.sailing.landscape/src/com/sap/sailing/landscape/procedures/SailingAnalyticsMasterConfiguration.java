package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sse.landscape.aws.AwsInstance;

public class SailingAnalyticsMasterConfiguration<ShardingKey,
HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>>
extends SailingAnalyticsApplicationConfiguration<ShardingKey, HostT> {
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, HostT>,
    ShardingKey,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>>
    extends SailingAnalyticsApplicationConfiguration.Builder<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey, HostT>, ShardingKey, HostT> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, HostT>,
    ShardingKey,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>>
    extends SailingAnalyticsApplicationConfiguration.BuilderImpl<BuilderT, SailingAnalyticsMasterConfiguration<ShardingKey, HostT>, ShardingKey, HostT>
    implements Builder<BuilderT, ShardingKey, HostT> {
        @Override
        public SailingAnalyticsMasterConfiguration<ShardingKey, HostT> build() throws Exception {
            return new SailingAnalyticsMasterConfiguration<ShardingKey, HostT>(this);
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey, HostT>,
    ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> Builder<BuilderT, ShardingKey, HostT> builder() {
        return new BuilderImpl<>();
    }

    protected SailingAnalyticsMasterConfiguration(BuilderImpl<?, ShardingKey, HostT> builder) {
        super(builder);
    }
}
