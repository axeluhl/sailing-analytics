package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.aws.ApplicationProcessHost;

/**
 * This procedure does two things: it {@link StartSailingAnalyticsHost starts} a {@link ApplicationProcessHost}, and
 * (currently implicitly, based on the way the /etc/init.d/sailing script works) also starts a "master" process that is
 * expected to have the default working directory {@code /home/sailing/servers/server} (see
 * {@link ApplicationProcessHost#DEFAULT_SERVER_PATH}).
 * <p>
 * 
 * TODO What we should probably be doing instead is harmonize the way the set-up / launching of a regular default master
 * works with how a {@link StartMultiServer multi-server is started}. We could start both empty, with only the default
 * reverse proxy mappings for {@code internal-server-status} and the plain access through the {@code ec2-...} hostname.
 * From there on, all process launching and stopping would work through the {@link DeployProcessOnMultiServer} procedure
 * (which then should be renamed to {@code DeployApplicationProcessOnServer}). That procedure then would have the
 *
 * @author Axel Uhl (D043530)
 */
public class StartSailingAnalyticsMaster<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey> {
    private static final String DEFAULT_MASTER_INSTANCE_NAME_SUFFIX = " (Master)";
    
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.Builder<BuilderT, StartSailingAnalyticsMaster<ShardingKey>, ShardingKey> {
    }
    
    // TODO model an AwsLandscape subclass describing the specifics of the Sailing landscape, with a central security service that a master replicates by default
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends StartSailingAnalyticsHost.BuilderImpl<BuilderT, StartSailingAnalyticsMaster<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        @Override
        public StartSailingAnalyticsMaster<ShardingKey> build() {
            return new StartSailingAnalyticsMaster<>(this);
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : super.getInstanceName() + DEFAULT_MASTER_INSTANCE_NAME_SUFFIX;
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey> Builder<BuilderT, ShardingKey> builder() {
        return new BuilderImpl<>();
    }

    protected StartSailingAnalyticsMaster(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
        addUserData(ProcessConfigurationVariable.USE_ENVIRONMENT, "live-master-server"); // TODO maybe this should be handled by this procedure adding the correct defaults, e.g., for replicating security/sharedsailing?
    }
}
