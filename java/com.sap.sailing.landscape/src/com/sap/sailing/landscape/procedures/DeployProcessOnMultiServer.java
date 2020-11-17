package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * Deploys a single {@link SailingAnalyticsProcess} to a given {@link ApplicationProcessHost} which ideally has been
 * launched using the {@link StartMultiServer} procedure, but may also work for hosts started in another way.<p>
 * 
 * TODO This is similar to {@link StartSailingAnalyticsHost} but doesn't need to fire up a new host. The assembly
 * of user data that {@link StartSailingAnalyticsHost} implements shall be factored and used here as well to append
 * to a barebones {@code env.sh} as obtained from a trivial installation. This trivial installation would start by
 * creating the {@link #serverName} folder under {@code /home/sailing/servers}, then copy the {@code refreshInstance.sh}
 * script there, run {@code ./refreshInstance.sh install-release}, then {@code ./refreshInstance.sh install-env environment-name},
 * then append specific variable values to the end of the {@code env.sh} file. In both cases, a set of variable assignments
 * needs to be generated which becomes the "user data" for {@link StartSailingAnalyticsHost} and becomes an appendix to
 * {@code env.sh} in this case.<p>
 * 
 * TODO Port assignments: this procedure needs to be able to scan the {@link #hostToDeployTo} for available ports in case
 * no ports are fixed upon construction of the procedure. The {@link #process} resulting from the deployment will tell which
 * ports were used eventually. The health check should be switched to using /gwt/status (which also tests the availability
 * of the Apache reverse proxy on the instance), hence all target groups can always use port 80/443 for traffic and health
 * check. The UDP and telnet ports do not need to be the same for master and replicas.<p>
 * 
 * TODO default the server directory to the SERVER_NAME property</p>
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class DeployProcessOnMultiServer<ShardingKey, HostT extends ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    private final Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> landscape;
    private final HostT hostToDeployTo;
    private final String serverName;
    
    /**
     * The process launched by this procedure. {@link #hostToDeployTo} is expected to be identical to
     * {@link SailingAnalyticsProcess#getHost() process.getHost()}.
     */
    private SailingAnalyticsProcess<ShardingKey> process;
    
    public DeployProcessOnMultiServer(Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> landscape,
            HostT hostToDeployTo, String serverName) {
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

    public HostT getHostToDeployTo() {
        return hostToDeployTo;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getLandscape() {
        return landscape;
    }
}
