package com.sap.sailing.landscape.procedures;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.impl.ApplicationProcessHostImpl;
import com.sap.sse.landscape.aws.orchestration.StartAwsApplicationHost;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * This launches an EC2 instance with a {@link SailingAnalyticsProcess} automatically started on it. The port configurations,
 * especially for the {@link Builder#getPort() HTTP port}, the {@link Builder#getTelnetPort() telnet port for OSGi console access}
 * and the {@link Builder#getExpeditionPort() "Expedition" UDP port} for this default process can be specified. They default to
 * 8888, 14888, and 2010, respectively.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public abstract class StartSailingAnalyticsHost<ShardingKey>
extends StartAwsApplicationHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>,
    StartFromSailingAnalyticsImage {
    private final static String INSTANCE_NAME_DEFAULT_PREFIX = "SL ";
    private final static String EXPEDITION_PORT_USER_DATA_NAME = "EXPEDITION_PORT";
    private final int DEFAULT_PORT = 8888;
    private final Integer port;
    private final String defaultServerDirectory;
    
    /**
     * The following defaults, in addition to the defaults implemented by the more general {@link StartAwsHost.Builder},
     * are:
     * <ul>
     * <li>If no {@link #setInstanceName(String) instance name} is provided, the instance name is constructed from the
     * {@link #getServerName() server name} by pre-pending the prefix "SL ".</li>
     * <li>Uses the latest machine image of the type described by
     * {@link StartSailingAnalyticsHost#IMAGE_TYPE_TAG_VALUE_SAILING} if no explicit
     * {@link #setMachineImage(AmazonMachineImage) machine image is set} and no {@link #setImageType(String) image type
     * is set} of which the latest version would be used otherwise.</li>
     * <li>If no {@link Release} is explicitly {@link #setRelease set}, or that {@link Optional} is empty,
     * {@link SailingReleaseRepository#INSTANCE}{@link SailingReleaseRepository#getLatestMasterRelease()
     * getLatestMasterRelease()} will be used instead.</li>
     * <li>The {@link #getDefaultServerDirectory() server directory} defaults to {code /home/sailing/servers/server}
     * (see {@link ApplicationProcessHost#DEFAULT_SERVER_PATH})</li>
     * </ul>
     * 
     * TODO the outbound replication configuration here or in the StartSailingAnalyticsMaster subclass should be based on the SERVER_NAME property
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey>
    extends StartAwsApplicationHost.Builder<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> {
        BuilderT setPort(int port);

        BuilderT setTelnetPort(int telnetPort);

        BuilderT setExpeditionPort(int expeditionPort);
        
        BuilderT setDefaultServerDirectory(String serverDirectory);
    }
    
    protected abstract static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey>
    extends StartAwsApplicationHost.BuilderImpl<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
    implements Builder<BuilderT, T, ShardingKey> {
        private Integer port;
        private Integer telnetPort;
        private Integer expeditionPort;
        private String defaultServerDirectory;
        
        @Override
        protected String getImageType() {
            return super.getImageType() == null ? IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        protected Optional<Release> getRelease() {
            return Optional.of(super.getRelease().orElse(SailingReleaseRepository.INSTANCE.getLatestMasterRelease()));
        }

        @Override
        protected String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : INSTANCE_NAME_DEFAULT_PREFIX+getServerName();
        }

        @Override
        protected HostSupplier<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> getHostSupplier() {
            return (String instanceId, AwsAvailabilityZone az, AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> landscape)->
                new ApplicationProcessHostImpl<>(instanceId, az, landscape,
                        (host, serverDirectory)->{
                            try {
                                return new SailingAnalyticsProcessImpl<ShardingKey>(host, serverDirectory);
                            } catch (NumberFormatException | JSchException | IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
        }
        
        protected Integer getPort() {
            return this.port;
        }
        
        @Override
        public BuilderT setPort(int port) {
            this.port = port;
            return self();
        }
        
        public Integer getTelnetPort() {
            return this.telnetPort;
        }
        
        @Override
        public BuilderT setTelnetPort(int telnetPort) {
            this.telnetPort = telnetPort;
            return self();
        }

        public Integer getExpeditionPort() {
            return expeditionPort;
        }
        
        @Override
        public BuilderT setExpeditionPort(int expeditionPort) {
            this.expeditionPort = expeditionPort;
            return self();
        }

        // TODO the host start-up should ideally be separated from the process installation/startup
        public String getDefaultServerDirectory() {
            return defaultServerDirectory == null ? ApplicationProcessHost.DEFAULT_SERVER_PATH : defaultServerDirectory;
        }

        @Override
        public BuilderT setDefaultServerDirectory(String defaultServerDirectory) {
            this.defaultServerDirectory = defaultServerDirectory;
            return self();
        }
    }
    
    protected StartSailingAnalyticsHost(BuilderImpl<?, ? extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey> builder) {
        super(builder);
        // remember the port we need in order to hand out the process
        this.port = builder.getPort();
        this.defaultServerDirectory = builder.getDefaultServerDirectory();
        if (builder.getPort() != null) {
            addUserData(ProcessConfigurationVariable.SERVER_PORT, builder.getPort().toString());
        }
        if (builder.getTelnetPort() != null) {
            addUserData(ProcessConfigurationVariable.TELNET_PORT, builder.getTelnetPort().toString());
        }
        if (builder.getExpeditionPort() != null) {
            addUserData(Collections.singleton(EXPEDITION_PORT_USER_DATA_NAME+"="+builder.getExpeditionPort()));
        }
    }
    
    protected int getPort() {
        return port == null ? DEFAULT_PORT : port;
    }
    
    protected String getDefaultServerDirectory() {
        return defaultServerDirectory;
    }
    
    public SailingAnalyticsProcess<ShardingKey> getSailingAnalyticsProcess() {
        return new SailingAnalyticsProcessImpl<>(getPort(), getHost(), getDefaultServerDirectory());
    }
}
