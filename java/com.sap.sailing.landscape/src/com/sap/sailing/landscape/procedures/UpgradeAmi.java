package com.sap.sailing.landscape.procedures;

import java.util.Optional;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.orchestration.Procedure;

import software.amazon.awssdk.services.ec2.model.InstanceType;

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
public class UpgradeAmi<ShardingKey, HostT extends SailingAnalyticsHost<ShardingKey>>
        extends
        StartAwsHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, SailingAnalyticsHost<ShardingKey>> implements
        Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private static final String IMAGE_UPGRADE = "image-upgrade";
    
    private MachineImage upgradedAmi;
    
    public UpgradeAmi(MachineImage machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups) {
        super(machineImage, landscape, instanceType, availabilityZone, keyName, securityGroups,
                Optional.of(Tags.with("Name", IMAGE_UPGRADE+" for "+machineImage.getId())), /* user data */ IMAGE_UPGRADE);
    }

    @Override
    public void run() {
        super.run(); // launches the machine in upgrade mode and shuts it down again, preparing for AMI creation
        // TODO now comes the waiting for the shutdown and initiating the creation of an AMI for the instance
        // TODO then comes the tagging of the volume snapshots created
        // TODO then set the resulting AMI to the 
    }
    
    /**
     * @return the resulting AMI that has the upgraded version of everything
     */
    public MachineImage getUpgradedAmi() {
        return upgradedAmi;
    }
}
