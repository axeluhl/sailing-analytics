package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * Deploys a single {@link SailingAnalyticsProcess} to a given {@link SailingAnalyticsHost} which ideally has been
 * launched using the {@link StartMultiServer} procedure, but may also work for hosts started in another way.<p>
 * 
 * TODO This is similar to {@link StartSailingAnalyticsHost} but doesn't need to fire up a new host. The assembly
 * of user data that {@link StartSailingAnalyticsHost} implements shall be factored and used here as well to append
 * to a barebones {@code env.sh} as obtained from a trivial installation. This trivial installation would start by
 * creating the {@link #serverName} folder under {@code /home/sailing/servers}, then copy the {@code refreshInstance.sh}
 * script there, run {@code ./refreshInstance.sh install-release}, then {@code ./refreshInstance.sh install-env environment-name},
 * then append specific variable values to the end of the {@code env.sh} file. In both cases, a set of variable assignments
 * needs to be generated which becomes the "user data" for {@link StartSailingAnalyticsHost} and becomes an appendix to
 * {@code env.sh} in this case.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class DeployProcessOnMultiServer<ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>> implements
        Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape;
    private final SailingAnalyticsHost<ShardingKey> hostToDeployTo;
    private final String serverName;
    
    /**
     * The process launched by this procedure. {@link #hostToDeployTo} is expected to be identical to
     * {@link SailingAnalyticsProcess#getHost() process.getHost()}.
     */
    private SailingAnalyticsProcess<ShardingKey> process;
    
    public DeployProcessOnMultiServer(Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            SailingAnalyticsHost<ShardingKey> hostToDeployTo, String serverName) {
        super();
        this.landscape = landscape;
        this.hostToDeployTo = hostToDeployTo;
        this.serverName = serverName;
    }
    
    @Override
    public void run() {
        // TODO Implement Runnable.run(...)
        
    }

    public SailingAnalyticsProcess<ShardingKey> getProcess() {
        return process;
    }

    public void setProcess(SailingAnalyticsProcess<ShardingKey> process) {
        this.process = process;
    }

    public SailingAnalyticsHost<ShardingKey> getHostToDeployTo() {
        return hostToDeployTo;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getLandscape() {
        return landscape;
    }
}
