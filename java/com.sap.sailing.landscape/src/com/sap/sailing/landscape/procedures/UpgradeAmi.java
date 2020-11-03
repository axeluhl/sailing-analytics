package com.sap.sailing.landscape.procedures;

import java.util.Collections;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.HostSupplier;
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
public class UpgradeAmi<ShardingKey,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
extends StartEmptyServer<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT> {
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
    public static interface Builder<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.Builder<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> {
    }

    protected static class BuilderImpl<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
    implements Builder<ShardingKey, MasterProcessT, ReplicaProcessT> {
        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return SailingAnalyticsHostImpl::new;
        }

        @Override
        public StartEmptyServer<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> build() {
            return new UpgradeAmi<>(this);
        }
    }
    
    public static <ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> Builder<ShardingKey, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }
    
    public UpgradeAmi(Builder<ShardingKey, MasterProcessT, ReplicaProcessT> builder) {
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
