package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
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
public class DeployProcessOnMultiServer<ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
ApplicationConfigurationT extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>,
ApplicationConfigurationBuilderT extends AwsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
extends AbstractProcedureImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    private final AwsInstance<ShardingKey, SailingAnalyticsMetrics> hostToDeployTo;
    private final ApplicationConfigurationT applicationConfiguration;
    
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>,
    ApplicationConfigurationBuilderT extends AwsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
    extends com.sap.sse.landscape.orchestration.Procedure.Builder<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
        BuilderT setHostToDeployTo(AwsInstance<ShardingKey, SailingAnalyticsMetrics> hostToDeployTo);
    }
    
    public static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>,
    ApplicationConfigurationBuilderT extends AwsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
    extends com.sap.sse.landscape.orchestration.AbstractProcedureImpl.BuilderImpl<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
    implements Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> {
        private ApplicationConfigurationBuilderT applicationConfigurationBuilder;
        private AwsInstance<ShardingKey, SailingAnalyticsMetrics> hostToDeployTo;

        @Override
        public DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> build() throws Exception {
            return new DeployProcessOnMultiServer<>(this);
        }

        /**
         * Expose to subclasses
         */
        @Override
        protected Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getLandscape() {
            return super.getLandscape();
        }

        @Override
        public BuilderT setHostToDeployTo(AwsInstance<ShardingKey, SailingAnalyticsMetrics> hostToDeployTo) {
            this.hostToDeployTo = hostToDeployTo;
            return self();
        }
        
        protected AwsInstance<ShardingKey, SailingAnalyticsMetrics> getHostToDeployTo() {
            return hostToDeployTo;
        }

        protected ApplicationConfigurationBuilderT getApplicationConfigurationBuilder() {
            return applicationConfigurationBuilder;
        }
    }
    
    /**
     * The process launched by this procedure. {@link #hostToDeployTo} is expected to be identical to
     * {@link SailingAnalyticsProcess#getHost() process.getHost()}.
     */
    private SailingAnalyticsProcess<ShardingKey> process;
    
    public DeployProcessOnMultiServer(BuilderImpl<?, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> builder) throws Exception {
        super(builder);
        this.hostToDeployTo = builder.getHostToDeployTo();
        this.applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
    }
    
    @Override
    public void run() {
        applicationConfiguration.getUserData(); // TODO assemble the user data to pipe to refreshInstance.sh auto-install-from-stdin
        // TODO ensure server name and directory are set
        // TODO work on defaulting things such as the ports based on other server installations on the same host and check applicationConfiguration for compatibility with ports from parallel installs
        // TODO Implement Runnable.run(...)
        
    }

    public SailingAnalyticsProcess<ShardingKey> getProcess() {
        return process;
    }

    public void setProcess(SailingAnalyticsProcess<ShardingKey> process) {
        this.process = process;
    }

    public AwsInstance<ShardingKey, SailingAnalyticsMetrics> getHostToDeployTo() {
        return hostToDeployTo;
    }
}
