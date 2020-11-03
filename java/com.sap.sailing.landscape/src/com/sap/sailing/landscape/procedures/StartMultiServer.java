package com.sap.sailing.landscape.procedures;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.ssh.SshCommandChannel;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Starts an empty multi-server. The image will cause a {@code /home/sailing/servers/server} directory to exist, but
 * after successfully launching, that directory will be removed. A {@link DeployProcessOnMultiServer} procedure needs to
 * be run with the {@link #getHost()} of this procedure telling the host on which to deploy the process.<p>
 * 
 * The implementation specializes the {@link UpgradeAmi} procedure in {@link Builder#setNoShutdown(boolean) no-shutdown} mode.
 * After running that part, the {@code httpd} service is launched.<p>
 * 
 * You want to at least specify an {@link Builder#setInstanceName(String) instance name} and {@link Builder#setInstanceType(InstanceType)}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class StartMultiServer<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartEmptyServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
    private static final Logger logger = Logger.getLogger(StartMultiServer.class.getName());
    private Optional<Duration> optionalTimeout;
    
    /**
     * Under all circumstances, this builder will return {@code true} for {@link #isNoShutdown()}, making sure
     * that after the upgrade progress the server does not try to re-boot.
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartEmptyServer.Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        @Override
        default boolean isNoShutdown() {
            return true;
        }
    }
    
    protected static class BuilderImpl<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
    implements Builder<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> {
        @Override
        public StartMultiServer<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> build() {
            return new StartMultiServer<>(this);
        }

        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return SailingAnalyticsHostImpl::new;
        }
    }
    
    public static <ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> Builder<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> builder() {
        return new BuilderImpl<>();
    }

    protected StartMultiServer(Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) {
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
