package com.sap.sailing.landscape.procedures;

import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.SailingAnalyticsProcessImpl;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.impl.ApplicationProcessHostImpl;
import com.sap.sse.landscape.aws.orchestration.StartAwsApplicationHost;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * This launches an EC2 instance with a {@link SailingAnalyticsProcess} automatically started on it. The port
 * configurations, especially for the {@link Builder#getPort() HTTP port}, the {@link Builder#getTelnetPort() telnet
 * port for OSGi console access} and the {@link Builder#getExpeditionPort() "Expedition" UDP port} for this default
 * process can be specified. They default to 8888, 14888, and 2010, respectively.
 * <p>
 * 
 * After launching the instance, the public key is also added to the {@code .ssh/authorized_keys} file of the user whose
 * name is provided by {@link #SAILING_USER_NAME}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public class StartSailingAnalyticsHost<ShardingKey>
extends StartAwsApplicationHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>,
    StartFromSailingAnalyticsImage {
    public static final Logger logger = Logger.getLogger(StartSailingAnalyticsHost.class.getName());
    private final static String INSTANCE_NAME_DEFAULT_PREFIX = "SL ";
    
    /**
     * The following defaults, in addition to the defaults implemented by the more general
     * {@link StartAwsApplicationHost.Builder}, are:
     * <ul>
     * <li>If no {@link #setInstanceName(String) instance name} is provided, the instance name is constructed from the
     * {@link #getServerName() server name} by pre-pending the prefix "SL ".</li>
     * <li>Uses the latest machine image of the type described by
     * {@link StartSailingAnalyticsHost#IMAGE_TYPE_TAG_VALUE_SAILING} if no explicit
     * {@link #setMachineImage(AmazonMachineImage) machine image is set} and no {@link #setImageType(String) image type
     * is set} of which the latest version would be used otherwise.</li>
     * <li>If no {@link Release} is explicitly {@link #setRelease set}, or that {@link Optional} is empty,
     * {@link SailingReleaseRepository#INSTANCE}{@link SailingReleaseRepository#getLatestMasterRelease()
     * .getLatestMasterRelease()} will be used instead.</li>
     * <li>The {@link #getServerDirectory() server directory} defaults to {@code /home/sailing/servers/server}
     * (see {@link ApplicationProcessHost#DEFAULT_SERVER_PATH})</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey>
    extends StartAwsApplicationHost.Builder<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> {
    }
    
    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, T, ShardingKey>,
    T extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey>
    extends StartAwsApplicationHost.BuilderImpl<BuilderT, T, ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>>
    implements Builder<BuilderT, T, ShardingKey> {
        protected BuilderImpl(SailingAnalyticsApplicationConfiguration.Builder<?, ?, ShardingKey> applicationConfigurationBuilder) {
            super(applicationConfigurationBuilder);
        }

        @Override
        protected SailingAnalyticsApplicationConfiguration.BuilderImpl<?, ?, ShardingKey> getApplicationConfigurationBuilder() {
            return (com.sap.sailing.landscape.procedures.SailingAnalyticsApplicationConfiguration.BuilderImpl<?, ?, ShardingKey>) super.getApplicationConfigurationBuilder();
        }

        @Override
        protected String getImageType() {
            return super.getImageType() == null ? IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        protected String getInstanceName() {
            return isInstanceNameSet() ? super.getInstanceName() : INSTANCE_NAME_DEFAULT_PREFIX+getApplicationConfigurationBuilder().getServerName();
        }

        @Override
        protected HostSupplier<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>, ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>> getHostSupplier() {
            return (String instanceId, AwsAvailabilityZone az, AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> landscape)->
                new ApplicationProcessHostImpl<>(instanceId, az, landscape,
                        (host, serverDirectory)->{
                            try {
                                return new SailingAnalyticsProcessImpl<ShardingKey>(host, serverDirectory, getOptionalTimeout(), Optional.of(getKeyName()), getPrivateKeyEncryptionPassphrase());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        }

        @Override
        public T build() throws Exception {
            @SuppressWarnings("unchecked")
            final T result = (T) new StartSailingAnalyticsHost<ShardingKey>(this);
            return result;
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, T, ShardingKey>, T extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey>
    BuilderT builder(SailingAnalyticsApplicationConfiguration.Builder<?, ?, ShardingKey> applicationConfigurationBuilder) {
        @SuppressWarnings("unchecked")
        final BuilderT result = (BuilderT) new BuilderImpl<BuilderT, T, ShardingKey>(applicationConfigurationBuilder);
        return result;
    }

    protected StartSailingAnalyticsHost(BuilderImpl<?, ? extends StartSailingAnalyticsHost<ShardingKey>, ShardingKey> builder) throws Exception {
        super(builder);
    }
    
    @Override
    protected SailingAnalyticsApplicationConfiguration<ShardingKey>
    getApplicationConfiguration() {
        return (SailingAnalyticsApplicationConfiguration<ShardingKey>) super.getApplicationConfiguration();
    }

    public SailingAnalyticsProcess<ShardingKey> getSailingAnalyticsProcess() {
        return new SailingAnalyticsProcessImpl<>(getApplicationConfiguration().getPort(), getHost(), getApplicationConfiguration().getServerDirectory());
    }
}
