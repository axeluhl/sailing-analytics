package com.sap.sailing.landscape.procedures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.AwsApplicationConfiguration;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;

public class SailingAnalyticsApplicationConfiguration<ShardingKey>
extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    /**
     * A builder that helps building an instance of type {@link SailingAnalyticsApplicationConfiguration} or any subclass thereof (then
     * using specialized builders). The following default rules apply, in addition to the defaults rules of the builders
     * that this builder interface {@link StartAwsHost.Builder extends}.
     * <ul>
     * <li>If no {@link #setPort(int) port} is provided, the {@link #DEFAULT_PORT} is used (8888).</li>
     * <li>If no {@link #setTelnetPort(int) telnet port} is provided, the {@link #DEFAULT_TELNET_PORT} is used (14888).</li>
     * <li>If no {@link #setExpeditionPort(int) expedition UDP port} is provided, the {@link #DEFAULT_EXPEDITION_PORT} is used (2010).</li>
     * <li>If no {@link #setServerDirectory(String) server directory} is specified, it defaults to {@link ApplicationProcessHost#DEFAULT_SERVER_PATH}.</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>, ShardingKey>
    extends AwsApplicationConfiguration.Builder<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
        int DEFAULT_PORT = 8888;
        int DEFAULT_TELNET_PORT = 14888;
        int DEFAULT_EXPEDITION_PORT = 2010;
        
        BuilderT setPort(int port);
        
        BuilderT setTelnetPort(int telnetPort);
        
        BuilderT setExpeditionPort(int expeditionPort);

        BuilderT setServerDirectory(String serverDirectory);
    }
    
    /**
     * The builder needs to know the {@link AwsRegion} in which the application will be run. In this region, discovery
     * of default database and messaging endpoints is performed.
     */
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>, ShardingKey>
    extends AwsApplicationConfiguration.BuilderImpl<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
    implements Builder<BuilderT, T, ShardingKey> {
        private Integer port;
        private Integer telnetPort;
        private Integer expeditionPort;
        private String serverDirectory;

        protected Integer getPort() {
            return port == null ? DEFAULT_PORT : port;
        }
        
        protected boolean isPortSet() {
            return port != null;
        }
        
        @Override
        public BuilderT setPort(int port) {
            this.port = port;
            return self();
        }
        
        protected Integer getTelnetPort() {
            return telnetPort == null ? DEFAULT_TELNET_PORT : telnetPort;
        }

        protected boolean isTelnetPortSet() {
            return telnetPort != null;
        }
        
        @Override
        public BuilderT setTelnetPort(int telnetPort) {
            this.telnetPort = telnetPort;
            return self();
        }

        protected Integer getExpeditionPort() {
            return expeditionPort == null ? DEFAULT_EXPEDITION_PORT : expeditionPort;
        }
        
        protected boolean isExpeditionPortSet() {
            return expeditionPort != null;
        }
        
        @Override
        public BuilderT setExpeditionPort(int expeditionPort) {
            this.expeditionPort = expeditionPort;
            return self();
        }

        protected String getServerDirectory() {
            return serverDirectory == null ? ApplicationProcessHost.DEFAULT_SERVERS_PATH + "/" + getServerName() : serverDirectory;
        }
        
        protected boolean isServerDirectorySet() {
            return serverDirectory != null;
        }

        @Override
        public BuilderT setServerDirectory(String serverDirectory) {
            this.serverDirectory = serverDirectory;
            return self();
        }

        @Override
        protected Optional<Release> getRelease() {
            return Optional.of(super.getRelease().orElse(SailingReleaseRepository.INSTANCE.getLatestMasterRelease()));
        }

        /**
         * Expose for callers in same package as this class
         */
        @Override
        protected String getServerName() {
            return super.getServerName();
        }

        /**
         * Expose for callers in same package as this class
         */
        @Override
        protected AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> getLandscape() {
            return super.getLandscape();
        }

        /**
         * Expose for callers in same package as this class
         */
        @Override
        protected AwsRegion getRegion() {
            return super.getRegion();
        }

        @Override
        protected Map<ProcessConfigurationVariable, String> getUserData() {
            final Map<ProcessConfigurationVariable, String> result = new HashMap<>(super.getUserData());
            addUserDataForPort(result, DefaultProcessConfigurationVariables.SERVER_PORT, getPort());
            addUserDataForPort(result, DefaultProcessConfigurationVariables.TELNET_PORT, getTelnetPort());
            addUserDataForPort(result, SailingProcessConfigurationVariables.EXPEDITION_PORT, getExpeditionPort());
            return result;
        }

        protected void addUserDataForPort(final Map<ProcessConfigurationVariable, String> result, ProcessConfigurationVariable variable, Integer port) {
            if (getPort() != null) {
                result.put(variable, port.toString());
            }
        }

        @Override
        public T build() throws Exception {
            @SuppressWarnings("unchecked")
            final T result = (T) new SailingAnalyticsApplicationConfiguration<ShardingKey>(this);
            return result;
        }
    }

    private final Integer port;
    private final Integer telnetPort;
    private final Integer expeditionPort;
    private final String serverDirectory;
    
    public static <BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends AwsApplicationConfiguration<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>, ShardingKey>
    BuilderT builder() {
        @SuppressWarnings("unchecked")
        final BuilderT result = (BuilderT) new BuilderImpl<BuilderT, T, ShardingKey>();
        return result;
    }

    protected SailingAnalyticsApplicationConfiguration(BuilderImpl<?, ?, ShardingKey> builder) {
        super(builder);
        this.port = builder.getPort();
        this.telnetPort = builder.getTelnetPort();
        this.expeditionPort = builder.getExpeditionPort();
        this.serverDirectory = builder.getServerDirectory();
    }

    protected Integer getPort() {
        return port;
    }

    protected Integer getTelnetPort() {
        return telnetPort;
    }

    protected Integer getExpeditionPort() {
        return expeditionPort;
    }

    protected String getServerDirectory() {
        return serverDirectory;
    }
}
