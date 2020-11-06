package com.sap.sailing.landscape.procedures;

import java.util.Collections;
import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.HostSupplier;
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
public abstract class StartSailingAnalyticsHost<ShardingKey, ProcessT extends SailingAnalyticsProcess<ShardingKey>>
extends StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final static String IMAGE_TYPE_TAG_VALUE_SAILING = "sailing-analytics-server";
    private final static String INSTANCE_NAME_DEFAULT_PREFIX = "SL ";
    private final static String EXPEDITION_PORT_USER_DATA_NAME = "EXPEDITION_PORT";
    private final int DEFAULT_PORT = 8888;
    private final Integer port;
    private final String defaultServerDirectory;
    
    /**
     * The following defaults, in addition to the defaults implemented by the more general {@link StartAwsHost.Builder},
     * are:
     * <ul>
     * <li>If no {@link #setInstanceName(String) instance name} is provided, the instance name is constructed from the {@link #getServerName() server name}
     * by pre-pending the prefix "SL ".</li>
     * <li>Uses the latest machine image of the type described by
     * {@link StartSailingAnalyticsHost#IMAGE_TYPE_TAG_VALUE_SAILING} if no explicit {@link #setMachineImage(AmazonMachineImage) machine image is set}
     * and no {@link #setImageType(String) image type is set} of which the latest version would be used otherwise.</li>
     * <li>If no {@link Release} is explicitly {@link #setRelease set}, or that {@link Optional} is empty,
     * {@link SailingReleaseRepository#INSTANCE}{@link SailingReleaseRepository#getLatestMasterRelease()
     * getLatestMasterRelease()} will be used instead.</li>
     * <li>The {@link #getDefaultServerDirectory() server directory} defaults to {@link /home/sailing/servers/server}</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<T extends StartSailingAnalyticsHost<ShardingKey, ProcessT>, ShardingKey, ProcessT extends SailingAnalyticsProcess<ShardingKey>>
    extends StartAwsHost.Builder<T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>> {
        Integer getPort();

        Builder<T, ShardingKey, ProcessT> setPort(int port);

        Integer getTelnetPort();

        Builder<T, ShardingKey, ProcessT> setTelnetPort(int telnetPort);

        Integer getExpeditionPort();

        Builder<T, ShardingKey, ProcessT> setExpeditionPort(int expeditionPort);
        
        String getDefaultServerDirectory();
        
        Builder<T, ShardingKey, ProcessT> setDefaultServerDirectory(String serverDirectory);
    }
    
    protected abstract static class BuilderImpl<T extends StartSailingAnalyticsHost<ShardingKey, ProcessT>, ShardingKey, ProcessT extends SailingAnalyticsProcess<ShardingKey>>
    extends StartAwsHost.BuilderImpl<T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>>
    implements Builder<T, ShardingKey, ProcessT> {
        private Integer port;
        private Integer telnetPort;
        private Integer expeditionPort;
        private String defaultServerDirectory;
        
        @Override
        public String getImageType() {
            return super.getImageType() == null ? StartSailingAnalyticsHost.IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        public Optional<Release> getRelease() {
            return Optional.of(super.getRelease().orElse(SailingReleaseRepository.INSTANCE.getLatestMasterRelease()));
        }

        @Override
        public String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : INSTANCE_NAME_DEFAULT_PREFIX+getServerName();
        }

        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return SailingAnalyticsHostImpl::new;
        }
        
        @Override
        public Integer getPort() {
            return this.port;
        }
        
        @Override
        public Builder<T, ShardingKey, ProcessT> setPort(int port) {
            this.port = port;
            return this;
        }
        
        @Override
        public Integer getTelnetPort() {
            return this.telnetPort;
        }
        
        @Override
        public Builder<T, ShardingKey, ProcessT> setTelnetPort(int telnetPort) {
            this.telnetPort = telnetPort;
            return this;
        }

        @Override
        public Integer getExpeditionPort() {
            return expeditionPort;
        }
        
        @Override
        public Builder<T, ShardingKey, ProcessT> setExpeditionPort(int expeditionPort) {
            this.expeditionPort = expeditionPort;
            return this;
        }

        // TODO the host start-up should ideally be separated from the process installation/startup
        @Override
        public String getDefaultServerDirectory() {
            return defaultServerDirectory == null ? "/home/sailing/servers/server" : defaultServerDirectory;
        }

        @Override
        public Builder<T, ShardingKey, ProcessT> setDefaultServerDirectory(String defaultServerDirectory) {
            this.defaultServerDirectory = defaultServerDirectory;
            return this;
        }
    }
    
    protected StartSailingAnalyticsHost(Builder<? extends StartSailingAnalyticsHost<ShardingKey, ProcessT>, ShardingKey, ProcessT> builder) {
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
    
    public abstract ProcessT getSailingAnalyticsProcess();
}
