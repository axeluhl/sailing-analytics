package com.sap.sailing.landscape.procedures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * Deploys a single {@link SailingAnalyticsProcess} to a given {@link ApplicationProcessHost} which ideally has been
 * launched using the {@link StartMultiServer} procedure, but may also work for hosts started in another way. The builder
 * has to be created using an {@link AwsApplicationConfiguration.Builder} which is used by the caller to define the key
 * properties of the application to be launched. From the server name property a default for the directory name to which
 * to deploy the application will be derived. If no ports are specified, the next available set of ports is determined
 * by looking at the other deployed processes and scanning for the next free port, starting from the default ports.<p>
 * 
 * @author Axel Uhl (D043530)
 */
public class DeployProcessOnMultiServer<ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
extends AbstractProcedureImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    private final ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo;
    private final ApplicationConfigurationT applicationConfiguration;
    private final Optional<Duration> optionalTimeout;

    /**
     * The process launched by this procedure. {@link #hostToDeployTo} is expected to be identical to
     * {@link SailingAnalyticsProcess#getHost() process.getHost()}.
     */
    private SailingAnalyticsProcess<ShardingKey> process;
    
    /**
     * When no explicit port specifications for HTTP, Telnet and Expedition/UDP have been provided by the application
     * configuration builder (which is the recommended default because otherwise conflicts with already deployed
     * processes are possible and likely), this builder will, upon {@link #build() building} the procedure, analyze the
     * ports already occupied by checking the already deployed processes and their configuration. For each of the three
     * port types (HTTP, Telnet, Expedition/UDP), the next available port starting from the respective default is
     * allocated in the application configuration.
     * <p>
     * 
     * The {@link SailingAnalyticsApplicationConfiguration#getServerDirectory() server directory} defaults to the
     * {@link {@link SailingAnalyticsApplicationConfiguration#getServerName() server name} appended to the
     * {@link ApplicationProcessHost#DEFAULT_SERVER_PATH default server directory}.<p>
     * 
     * The {@link SailingAnalyticsApplicationConfiguration.Builder#setServerName(String) server name} must have been set
     * on the application configuration builder before invoking this builder's {@link #build()} method.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
    ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
    extends com.sap.sse.landscape.orchestration.Procedure.Builder<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
        BuilderT setHostToDeployTo(ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo);
    }
    
    public static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
    ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
    extends com.sap.sse.landscape.orchestration.AbstractProcedureImpl.BuilderImpl<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
    implements Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> {
        private final SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> applicationConfigurationBuilder;
        private ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo;

        protected BuilderImpl(SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> applicationConfigurationBuilder) {
            super();
            this.applicationConfigurationBuilder = applicationConfigurationBuilder;
        }
        
        @Override
        public DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> build() throws Exception {
            assert getHostToDeployTo() != null;
            assert getApplicationConfigurationBuilder().getServerName() != null;
            if (getApplicationConfigurationBuilder().getServerDirectory() == null) {
                getApplicationConfigurationBuilder().setServerDirectory(ApplicationProcessHost.DEFAULT_SERVER_PATH+"/"+getApplicationConfigurationBuilder().getServerName());
            }
            final Iterable<SailingAnalyticsProcess<ShardingKey>> applicationProcesses = getHostToDeployTo().getApplicationProcesses();
            if (!getApplicationConfigurationBuilder().isPortSet()) {
                getApplicationConfigurationBuilder().setPort(getNextAvailablePort(applicationProcesses,
                        SailingAnalyticsApplicationConfiguration.Builder.DEFAULT_PORT,
                        SailingAnalyticsProcess::getPort));
            }
            if (!getApplicationConfigurationBuilder().isTelnetPortSet()) {
                getApplicationConfigurationBuilder().setTelnetPort(getNextAvailablePort(applicationProcesses,
                        SailingAnalyticsApplicationConfiguration.Builder.DEFAULT_TELNET_PORT,
                        ap->{
                            try {
                                return ap.getTelnetPortToOSGiConsole(getOptionalTimeout());
                            } catch (NumberFormatException | JSchException | IOException | SftpException
                                    | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
            if (!getApplicationConfigurationBuilder().isExpeditionPortSet()) {
                getApplicationConfigurationBuilder().setExpeditionPort(getNextAvailablePort(applicationProcesses,
                        SailingAnalyticsApplicationConfiguration.Builder.DEFAULT_EXPEDITION_PORT,
                        ap->{
                            try {
                                return ap.getExpeditionUdpPort(getOptionalTimeout());
                            } catch (NumberFormatException | JSchException | IOException | InterruptedException
                                    | SftpException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
            return new DeployProcessOnMultiServer<>(this);
        }

        private int getNextAvailablePort(final Iterable<SailingAnalyticsProcess<ShardingKey>> applicationProcesses, int defaultPort, Function<SailingAnalyticsProcess<ShardingKey>, Integer> portFetcher) {
            final Set<Integer> occupiedPorts = new HashSet<>();
            for (final SailingAnalyticsProcess<ShardingKey> applicationProcess : applicationProcesses) {
                occupiedPorts.add(portFetcher.apply(applicationProcess));
            }
            int port = defaultPort;
            while (port<Integer.MAX_VALUE && occupiedPorts.contains(port)) {
                port++;
            }
            return port;
        }

        /**
         * Expose to subclasses
         */
        @Override
        protected Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getLandscape() {
            return super.getLandscape();
        }

        @Override
        public BuilderT setHostToDeployTo(ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo) {
            this.hostToDeployTo = hostToDeployTo;
            return self();
        }
        
        protected ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getHostToDeployTo() {
            return hostToDeployTo;
        }

        protected SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> getApplicationConfigurationBuilder() {
            return applicationConfigurationBuilder;
        }
        
        /**
         * Make visible in package
         */
        @Override
        protected Optional<Duration> getOptionalTimeout() {
            return super.getOptionalTimeout();
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
    ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
    Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> builder(ApplicationConfigurationBuilderT applicationConfigurationBuilder) {
        @SuppressWarnings("unchecked")
        final SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> applicationConfigurationBuilderCast =
                (SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>) applicationConfigurationBuilder;
        return new BuilderImpl<>(applicationConfigurationBuilderCast);
    }
    
    protected DeployProcessOnMultiServer(BuilderImpl<?, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> builder) throws Exception {
        super(builder);
        this.hostToDeployTo = builder.getHostToDeployTo();
        this.optionalTimeout = builder.getOptionalTimeout();
        this.applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
        assert getHostToDeployTo() != null;
    }
    
    @Override
    public void run() throws IOException, InterruptedException, JSchException, SftpException {
        assert getHostToDeployTo() != null;
        final String serverDirectory = applicationConfiguration.getServerDirectory();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        getHostToDeployTo().createRootSshChannel(optionalTimeout).sendCommandLineSynchronously(
                "mkdir -p "+serverDirectory+"; "+
                "cd "+serverDirectory+"; "+
                "echo '"+applicationConfiguration.getUserData()+
                "' | /home/sailing/code/java/target/refreshInstance.sh auto-install-from-stdin", stderr);
        process = new SailingAnalyticsProcessImpl<>(applicationConfiguration.getPort(), getHostToDeployTo(), serverDirectory);
    }
    
    public SailingAnalyticsProcess<ShardingKey> getProcess() {
        return process;
    }

    private ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getHostToDeployTo() {
        return hostToDeployTo;
    }
}
