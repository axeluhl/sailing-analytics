package com.sap.sailing.landscape.procedures;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.orchestration.AbstractProcedureImpl;
import com.sap.sse.landscape.orchestration.Procedure;
import com.sap.sse.landscape.ssh.SshCommandChannel;

/**
 * Deploys a single {@link SailingAnalyticsProcess} to a given {@link ApplicationProcessHost} which ideally has been
 * launched using the {@link StartMultiServer} procedure, but may also work for hosts started in another way. The builder
 * has to be created using an {@link AwsApplicationConfiguration.Builder} which is used by the caller to define the key
 * properties of the application to be launched. From the server name property a default for the directory name to which
 * to deploy the application will be derived. If no ports are specified, the next available set of ports is determined
 * by looking at the other deployed processes and scanning for the next free port, starting from the default ports.<p>
 * 
 * The application is started after deployment as part of this procedure, and a default "Home-SSL" reverse proxy mapping is established
 * for a hostname <tt>${SERVER_NAME}.sapsailing.com</tt>.
 * 
 * @author Axel Uhl (D043530)
 */
public class DeployProcessOnMultiServer<ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
extends AbstractProcedureImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    private static final Logger logger = Logger.getLogger(DeployProcessOnMultiServer.class.getName());
    private final ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo;
    private final ApplicationConfigurationT applicationConfiguration;
    private final Optional<Duration> optionalTimeout;
    private byte[] privateKeyEncryptionPassphrase;

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
     * {@link ApplicationProcessHost#DEFAULT_SERVER_PATH default server directory}.
     * <p>
     * 
     * The {@link SailingAnalyticsApplicationConfiguration.Builder#setServerName(String) server name} must have been set
     * on the application configuration builder before invoking this builder's {@link #build()} method.
     * <p>
     * 
     * If no {@link Builder#setLandscape(Landscape) landscape} is explicitly set, the
     * {@link AwsApplicationConfiguration.Builder#setLandscape(AwsLandscape) landscape} of the application configuration
     * is used if provided, otherwise the {@link ApplicationProcessHost#getLandscape() landscape} of the
     * {@link #setHostToDeployTo(ApplicationProcessHost) host} is used as a default. The latter is then also used as the
     * default for the application configuration if no landscape has been provided for it explicitly.
     * <p>
     * 
     * If the application configuration does not
     * {@link AwsApplicationConfiguration.Builder#setRegion(com.sap.sse.landscape.aws.impl.AwsRegion) specify a region},
     * the host's region is used as the default region.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
    ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
    extends com.sap.sse.landscape.orchestration.Procedure.Builder<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
        BuilderT setHostToDeployTo(ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo);
        BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase);
    }
    
    public static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>,
    ApplicationConfigurationT extends SailingAnalyticsApplicationConfiguration<ShardingKey>,
    ApplicationConfigurationBuilderT extends SailingAnalyticsApplicationConfiguration.Builder<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey>>
    extends com.sap.sse.landscape.orchestration.AbstractProcedureImpl.BuilderImpl<BuilderT, DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT>, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
    implements Builder<BuilderT, ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> {
        private final SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> applicationConfigurationBuilder;
        private ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> hostToDeployTo;
        private byte[] privateKeyEncryptionPassphrase;

        protected BuilderImpl(SailingAnalyticsApplicationConfiguration.BuilderImpl<ApplicationConfigurationBuilderT, ApplicationConfigurationT, ShardingKey> applicationConfigurationBuilder) {
            super();
            this.applicationConfigurationBuilder = applicationConfigurationBuilder;
        }
        
        @Override
        public DeployProcessOnMultiServer<ShardingKey, HostT, ApplicationConfigurationT, ApplicationConfigurationBuilderT> build() throws Exception {
            assert getHostToDeployTo() != null;
            assert getApplicationConfigurationBuilder().getServerName() != null;
            if (!getApplicationConfigurationBuilder().isServerDirectorySet()) {
                getApplicationConfigurationBuilder().setServerDirectory(ApplicationProcessHost.DEFAULT_SERVERS_PATH+"/"+getApplicationConfigurationBuilder().getServerName());
            }
            final Iterable<SailingAnalyticsProcess<ShardingKey>> applicationProcesses = getHostToDeployTo().getApplicationProcesses(getOptionalTimeout(), privateKeyEncryptionPassphrase);
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
                                return ap.getTelnetPortToOSGiConsole(getOptionalTimeout(), privateKeyEncryptionPassphrase);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
            if (!getApplicationConfigurationBuilder().isExpeditionPortSet()) {
                getApplicationConfigurationBuilder().setExpeditionPort(getNextAvailablePort(applicationProcesses,
                        SailingAnalyticsApplicationConfiguration.Builder.DEFAULT_EXPEDITION_PORT,
                        ap->{
                            try {
                                return ap.getExpeditionUdpPort(getOptionalTimeout(), privateKeyEncryptionPassphrase);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
            if (getLandscape() == null) {
                if (getApplicationConfigurationBuilder().getLandscape() != null) {
                    setLandscape(getApplicationConfigurationBuilder().getLandscape());
                } else {
                    setLandscape(getHostToDeployTo().getLandscape());
                }
            }
            if (getApplicationConfigurationBuilder().getLandscape() == null) {
                getApplicationConfigurationBuilder().setLandscape(getLandscape());
            }
            if (getApplicationConfigurationBuilder().getRegion() == null) {
                getApplicationConfigurationBuilder().setRegion(getHostToDeployTo().getRegion());
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
        protected AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getLandscape() {
            return (AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>) super.getLandscape();
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

        @Override
        public BuilderT setPrivateKeyEncryptionPassphrase(byte[] privateKeyEncryptionPassphrase) {
            this.privateKeyEncryptionPassphrase = privateKeyEncryptionPassphrase;
            return self();
        }
        
        protected byte[] getPrivateKeyEncryptionPassphrase() {
            return privateKeyEncryptionPassphrase;
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
        this.privateKeyEncryptionPassphrase = builder.getPrivateKeyEncryptionPassphrase();
        this.applicationConfiguration = builder.getApplicationConfigurationBuilder().build();
        assert getHostToDeployTo() != null;
    }
    
    @Override
    public void run() throws Exception {
        assert getHostToDeployTo() != null;
        final String serverDirectory = applicationConfiguration.getServerDirectory();
        {
            final SshCommandChannel sshChannel = getHostToDeployTo().createSshChannel("sailing", optionalTimeout, privateKeyEncryptionPassphrase);
            logger.info("stdout: "+sshChannel.runCommandAndReturnStdoutAndLogStderr(
                    "mkdir -p "+serverDirectory+"; "+
                    "cd "+serverDirectory+"; "+
                    "echo '"+applicationConfiguration.getAsEnvironmentVariableAssignments()+
                    "' | /home/sailing/code/java/target/refreshInstance.sh auto-install-from-stdin; ./start; ./defineReverseProxyMappings.sh",
                    "stderr: ", Level.WARNING));
        }
        {
            final SshCommandChannel sshChannel = getHostToDeployTo().createRootSshChannel(optionalTimeout, privateKeyEncryptionPassphrase);
            logger.info("stdout: "+sshChannel.runCommandAndReturnStdoutAndLogStderr("service httpd reload", "stderr: ", Level.WARNING));
        }
        process = new SailingAnalyticsProcessImpl<>(applicationConfiguration.getPort(), getHostToDeployTo(), serverDirectory);
    }
    
    public SailingAnalyticsProcess<ShardingKey> getProcess() {
        return process;
    }

    private ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getHostToDeployTo() {
        return hostToDeployTo;
    }
}
