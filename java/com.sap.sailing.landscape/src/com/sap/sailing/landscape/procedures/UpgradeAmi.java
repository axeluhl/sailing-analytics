package com.sap.sailing.landscape.procedures;

import java.util.Collections;

import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.orchestration.Procedure;

/**
 * Upgrades an existing Amazon Machine Image that is expected to be prepared for such an upgrade, by
 * invoking it with very specific user data that trigger the automatic upgrade. The resulting AMI can
 * be obtained after this procedure has completed by calling {@link #getUpgradedAmi()}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class UpgradeAmi<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartAwsHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private static final String IMAGE_UPGRADE_USER_DATA = "image-upgrade";
    private static final String NO_SHUTDOWN_USER_DATA = "no-shutdown";
    
    private MachineImage upgradedAmi;
    
    /**
     * Additional default rules in addition to what the {@link StartAwsHost.Builder parent builder} defines:
     * 
     * <ul>
     * <li>If no {@link #getInstanceName() instance name} is set, the default instance name will be constructed as
     * {@code IMAGE_UPGRADE+" for "+machineImage.getId()}</li>
     * <li>The user data are set to the string defined by {@link UpgradeAmi#IMAGE_UPGRADE_USER_DATA}, forcing the image to
     * boot without trying to launch a process instance.</li>
     * </ul>
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartAwsHost.Builder<UpgradeAmi<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        boolean isNoShutdown();
        
        Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setNoShutdown(boolean noShutdown);
    }

    protected static class BuilderImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>>
    extends StartAwsHost.BuilderImpl<UpgradeAmi<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>
    implements Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
        private boolean noShutdown;
        
        @Override
        public UpgradeAmi<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> build() {
            return new UpgradeAmi<>(this);
        }

        @Override
        public boolean isNoShutdown() {
            return noShutdown;
        }

        @Override
        public Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> setNoShutdown(boolean noShutdown) {
            this.noShutdown = noShutdown;
            return this;
        }

        @Override
        public String getInstanceName() {
            return super.getInstanceName() == null ? IMAGE_UPGRADE_USER_DATA+" for "+getMachineImage().getId() : super.getInstanceName();
        }
    }
    
    public static <ShardingKey, MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, MetricsT>> Builder<ShardingKey, MetricsT, MasterProcessT,ReplicaProcessT, HostT> builder() {
        return new BuilderImpl<>();
    }
    
    public UpgradeAmi(Builder<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) {
        super(builder);
        addUserData(Collections.singleton(IMAGE_UPGRADE_USER_DATA));
        if (builder.isNoShutdown()) {
            addUserData(Collections.singleton(NO_SHUTDOWN_USER_DATA));
        }
    }

    @Override
    public void run() throws Exception {
        super.run(); // launches the machine in upgrade mode and shuts it down again, preparing for AMI creation
        // TODO now comes the waiting for the shutdown and initiating the creation of an AMI for the instance
        // TODO then comes the tagging of the volume snapshots created
        // TODO then tag the resulting AMI according to the original image's tags, except for the name where automatic version number increment should be implemented
    }
    
    /**
     * @return the resulting AMI that has the upgraded version of everything
     */
    public MachineImage getUpgradedAmi() {
        return upgradedAmi;
    }
}
