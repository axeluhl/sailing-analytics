package com.sap.sailing.landscape.procedures;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;
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
public class StartMultiServer<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends StartEmptyServer<StartMultiServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>>
implements StartFromSailingAnalyticsImage {
    private static final Logger logger = Logger.getLogger(StartMultiServer.class.getName());
    private Optional<Duration> optionalTimeout;
    
    /**
     * Under all circumstances, this builder will return {@code true} for {@link #isNoShutdown()}, making sure
     * that after the upgrade progress the server does not try to re-boot. Defaults:<ul>
     * <li>The instance name defaults to "Multi-Server"</li>
     * <li>The instance type defaults to {@link InstanceType#C5_D_4_XLARGE}</li>
     * </ul>
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.Builder<StartMultiServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> {
    }
    
    protected static class BuilderImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<StartMultiServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>>
    implements Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
        @Override
        public StartMultiServer<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> build() {
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
        protected String getInstanceName() {
            final String result;
            if (isInstanceNameSet()) {
                result = super.getInstanceName();
            } else {
                result = "Multi-Server";
            }
            return result;
        }
        
        @Override
        protected HostSupplier<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> getHostSupplier() {
            final HostSupplier<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, AwsInstance<ShardingKey, MetricsT>> result;
            if (super.getHostSupplier() == null) {
                result = AwsInstanceImpl::new;
            } else {
                result = super.getHostSupplier();
            }
            return result;
        }
        
        @Override
        protected InstanceType getInstanceType() {
            final InstanceType result;
            if (super.getInstanceType() == null) {
                result = InstanceType.C5_D_4_XLARGE;
            } else {
                result = super.getInstanceType();
            }
            return result;
        }
    }
    
    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }

    protected StartMultiServer(BuilderImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> builder) {
        super(builder);
        this.optionalTimeout = builder.getOptionalTimeout();
    }
    
    @Override
    public void run() throws Exception {
        super.run();
        final String instanceId = getHost().getInstanceId();
        final SshCommandChannel sshCommandChannel = getHost().createRootSshChannel(optionalTimeout);
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        sshCommandChannel.sendCommandLineSynchronously("rm -rf "+SailingAnalyticsHost.DEFAULT_SERVER_PATH+"; service httpd start", stderr);
        logger.info("stdout for removing "+SailingAnalyticsHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance "+instanceId+": "+sshCommandChannel.getStreamContentsAsString());
        logger.info("stderr for removing "+SailingAnalyticsHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance \"+instanceId+\": "+stderr.toString());
        logger.info("exit status for removing "+SailingAnalyticsHost.DEFAULT_SERVER_PATH+" and starting httpd service on instance \"+instanceId+\": "+sshCommandChannel.getExitStatus());
    }
}
