package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.impl.SailingAnalyticsMasterImpl;
import com.sap.sse.landscape.ProcessConfigurationVariable;

/**
 * This procedure does two things: it {@link StartSailingAnalyticsHost starts} a {@link SailingAnalyticsHost}, and
 * (currently implicitly, based on the way the /etc/init.d/sailing script works) also starts a "master" process
 * that is expected to have the default working directory {@code /home/sailing/servers/server}.<p>
 * 
 * TODO What we should probably be doing instead is harmonize the way the set-up / launching of a regular default master
 * works with how a {@link StartMultiServer multi-server is started}. We could start both empty, with only the default
 * reverse proxy mappings for {@code internal-server-status} and the plain access through the {@code ec2-...} hostname.
 * From there on, all process launching and stopping would work through the {@link DeployProcessOnMultiServer} procedure
 * (which then should be renamed to {@code DeployApplicationProcessOnServer}). That procedure then would have the 
 *
 * @author Axel Uhl (D043530)
 */
public class StartSailingAnalyticsMaster<ShardingKey>
        extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsMaster<ShardingKey>> {
    private static final String DEFAULT_MASTER_INSTANCE_NAME_SUFFIX = " (Master)";
    
    public static interface Builder<ShardingKey>
    extends StartSailingAnalyticsHost.Builder<StartSailingAnalyticsMaster<ShardingKey>, ShardingKey, SailingAnalyticsMaster<ShardingKey>> {
    }
    
    // TODO model an AwsLandscape subclass describing the specifics of the Sailing landscape, with a central security service that a master replicates by default
    
    protected static class BuilderImpl<ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<StartSailingAnalyticsMaster<ShardingKey>, ShardingKey, SailingAnalyticsMaster<ShardingKey>>
    implements Builder<ShardingKey> {
        @Override
        public StartSailingAnalyticsMaster<ShardingKey> build() {
            return new StartSailingAnalyticsMaster<ShardingKey>(this);
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : super.getInstanceName() + DEFAULT_MASTER_INSTANCE_NAME_SUFFIX;
        }
    }
    
    public static <ShardingKey> Builder<ShardingKey> builder() {
        return new BuilderImpl<>();
    }

    protected StartSailingAnalyticsMaster(BuilderImpl<ShardingKey> builder) {
        super(builder);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-master-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }
    
    @Override
    public SailingAnalyticsMaster<ShardingKey> getSailingAnalyticsProcess() {
        return new SailingAnalyticsMasterImpl<>(getPort(), getHost(), getDefaultServerDirectory());
    }
}
