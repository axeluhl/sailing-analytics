package com.sap.sailing.landscape.procedures;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.ssh.SshCommandChannel;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Starts an empty multi-server. The image will cause a {@code /home/sailing/servers/server} directory to exist, but
 * after successfully launching, that directory will be removed. A {@link DeployProcessOnMultiServer} procedure needs to
 * be run with the {@link #getHost()} of this procedure telling the host on which to deploy the process.<p>
 * 
 * The implementation specializes the {@link StartEmptyServer} procedure in {@link Builder#setNoShutdown(boolean) no-shutdown} mode.
 * After running that part, the {@code httpd} service is launched.<p>
 * 
 * You want to at least specify an {@link Builder#setInstanceName(String) instance name} and {@link Builder#setInstanceType(InstanceType)}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <SailingAnalyticsHost<ShardingKey>>
 */
public class StartMultiServer<ShardingKey,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
extends StartEmptyServer<StartMultiServer<ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
implements StartFromSailingAnalyticsImage {
    private static final Logger logger = Logger.getLogger(StartMultiServer.class.getName());
    private Optional<Duration> optionalTimeout;
    
    /**
     * Under all circumstances, this builder will return {@code true} for {@link #isNoShutdown()}, making sure
     * that after the upgrade progress the server does not try to re-boot.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.Builder<StartMultiServer<ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> {
    }
    
    protected static class BuilderImpl<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<StartMultiServer<ShardingKey, MasterProcessT, ReplicaProcessT>,
    ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
    implements Builder<ShardingKey, MasterProcessT, ReplicaProcessT> {
        @Override
        public StartMultiServer<ShardingKey, MasterProcessT, ReplicaProcessT> build() {
            return new StartMultiServer<>(this);
        }

        @Override
        protected boolean isNoShutdown() {
            return true;
        }

        @Override
        protected String getImageType() {
            return super.getImageType() == null ? IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return new HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>() {
                @Override
                public SailingAnalyticsHost<ShardingKey> supply(String instanceId, AwsAvailabilityZone az,
                        AwsLandscape<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT> landscape) {
                    return null;
                }
            };
        }
    }
    
    public static <ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    Builder<ShardingKey, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }

    protected StartMultiServer(BuilderImpl<ShardingKey, MasterProcessT, ReplicaProcessT> builder) {
        super(builder);
        this.optionalTimeout = builder.getOptionalTimeout();
    }
    
    @Override
    public void run() throws Exception {
        super.run();
        final SshCommandChannel sshCommandChannel = getHost().createRootSshChannel(optionalTimeout);
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        sshCommandChannel.sendCommandLineSynchronously("service httpd start", stderr);
        final String instanceId = getHost().getInstanceId();
        logger.info("stdout for starting httpd service on instance "+instanceId+": "+sshCommandChannel.getStreamContentsAsString());
        logger.info("stderr for starting httpd service on instance \"+instanceId+\": "+stderr.toString());
        logger.info("exit status for starting httpd service on instance \"+instanceId+\": "+sshCommandChannel.getExitStatus());
    }
}
