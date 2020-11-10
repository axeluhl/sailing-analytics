package com.sap.sailing.landscape.procedures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.HostSupplier;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.landscape.orchestration.Procedure;

import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.ImageState;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

/**
 * Upgrades an existing Amazon Machine Image that is expected to be prepared for such an upgrade, by
 * invoking it with very specific user data that trigger the automatic upgrade. The resulting AMI can
 * be obtained after this procedure has completed by calling {@link #getUpgradedAmi()}.<p>
 * 
 * The procedure uses a new name for the image and its snapshots. This name can either be explicitly
 * configured using the {@link Builder}, or it is inferred from the name of the image to upgrade by looking
 * for a version number in the form {@code [0-9]+\.[0-9]+(\.[0-9]+)}, so that major.minor and major.minor.micro
 * will all be considered matches. The {@link Builder} can be provided only with a new version string. By default,
 * it will increment the last version part matched (minor in case of major.minor and micro in case of
 * major.minor.micro) by one.<p>
 * 
 * TODO generalize such that kernel upgrades / security patches can be applied to any CentOS/Amazon Linux-based
 * image; then one level more specialized clean httpd logs for anything that has a reverse proxy on it;
 * then for those that have a default server process / git on it cleaning that up and refreshing...
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <HostT>
 */
public class UpgradeAmi<ShardingKey,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
extends StartEmptyServer<UpgradeAmi<ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>, StartFromSailingAnalyticsImage {
    private static final Logger logger = Logger.getLogger(UpgradeAmi.class.getName());
    private static final Pattern imageNamePattern = Pattern.compile("^(.*) ([0-9]+)\\.([0-9]+)(\\.([0-9]+))?$");
    
    private final String upgradedImageName;
    private final Duration timeout;
    private final boolean waitForShutdown; // no need to wait if no shutdown was requested
    private final Map<String, String> deviceNamesToSnapshotBaseNames;
    private AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> upgradedAmi;
    
    /**
     * Additional default rules in addition to what the {@link StartAwsHost.Builder parent builder} defines:
     * 
     * <ul>
     * <li>If no {@link #getInstanceName() instance name} is set, the default instance name will be constructed as
     * {@code IMAGE_UPGRADE+" for "+machineImage.getId()}</li>
     * <li>The user data are set to the string defined by {@link UpgradeAmi#IMAGE_UPGRADE_USER_DATA}, forcing the image to
     * boot without trying to launch a process instance.</li>
     * <li>The {@link #isNoShutdown()} property default is changed to {@code false} because when upgrading an image the
     * default is that after the upgrading activity the instance is shut down for image creation.</li>
     * <li>By default the last part of the version number found will be incremented.</li>
     * <li>If the {@link #setVersionPartToIncrement(VersionPart)} method is used to explicitly specify a part to increment
     * then if its {@link VersionPart#MICRO} and the {@link VersionPart#MICRO} part doesn't exist yet, it is added and set to 0.
     * If the part exists it is incremented and in case it's not the last part, tailing parts are all set to 0. With this,
     * the new image name defaults to the old image's base name plus a space plus the new version number.</li>
     * </ul>
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<BuilderT extends Builder<BuilderT, ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.Builder<BuilderT, UpgradeAmi<ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> {
        enum VersionPart {
            MAJOR, MINOR, MICRO
        }

        BuilderT setUpgradedImageName(String upgradedImageName);

        BuilderT setVersionPartToIncrement(VersionPart versionPartToIncrement);
        
        /**
         * It is possible to assign base names for snapshots based on their device name in the AMI. For example, "/dev/sdc" may
         * be the "Swap" device, and "/dev/xvda" may be the "System" partition. The full name for the snapshot is then assembled from
         * the AMI's name including its version and this base name. If no such basename is provided for a device name for which
         * a block device mapping to a snapshot exists, the snapshot will only be named after the AMI's name, so when multiple snapshots
         * are connected to the AMI then their names will not be discernible.
         */
        BuilderT setSnapshotBaseName(String deviceName, String snapshotBaseName);
    }

    protected static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<BuilderT, UpgradeAmi<ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
    implements Builder<BuilderT, ShardingKey, MasterProcessT, ReplicaProcessT> {
        private String upgradedImageName;
        private VersionPart versionPartToIncrement;
        private final Map<String, String> deviceNamesToSnapshotBaseNames;

        private BuilderImpl() {
            super();
            // TODO the following is very specific to the way the Sailing Analytics image has been set up;
            // TODO encapsulate with other image specificities required for upgrading?
            deviceNamesToSnapshotBaseNames = new HashMap<>();
            setSnapshotBaseName("/dev/xvda", "System");
            setSnapshotBaseName("/dev/sdc", "Swap");
            setSnapshotBaseName("/dev/sdf", "Home");
            setNoShutdown(false);
        }
        
        @Override
        protected String getImageType() {
            return super.getImageType() == null ? IMAGE_TYPE_TAG_VALUE_SAILING : super.getImageType();
        }

        @Override
        public BuilderT setVersionPartToIncrement(VersionPart versionPartToIncrement) {
            this.versionPartToIncrement = versionPartToIncrement;
            return self();
        }

        private String increaseVersionNumber(String imageName) {
            final String result;
            final Matcher versionNumberMatcher = imageNamePattern.matcher(imageName);
            if (versionNumberMatcher.matches()) {
                final String imageBaseName = versionNumberMatcher.group(1);
                final Integer oldMajorVersion = Integer.valueOf(versionNumberMatcher.group(2));
                final Integer oldMinorVersion = Integer.valueOf(versionNumberMatcher.group(3));
                final Integer oldMicroVersion = versionNumberMatcher.group(5) == null ? null : Integer.valueOf(versionNumberMatcher.group(5));
                final VersionPart partToEffectivelyIncrement = versionPartToIncrement == null
                        ? oldMicroVersion == null ? VersionPart.MINOR : VersionPart.MICRO
                        : versionPartToIncrement;
                final Integer newMajorVersion = partToEffectivelyIncrement == VersionPart.MAJOR ? oldMajorVersion + 1 : oldMajorVersion;
                final Integer newMinorVersion = partToEffectivelyIncrement == VersionPart.MINOR ? oldMinorVersion + 1 : oldMinorVersion;
                final Integer newMicroVersion = oldMicroVersion == null
                        ? partToEffectivelyIncrement == VersionPart.MICRO
                            ? Integer.valueOf(0)
                            : null
                        : partToEffectivelyIncrement == VersionPart.MICRO
                            ? Integer.valueOf(oldMicroVersion + 1)
                            : oldMicroVersion;
                final StringBuilder sb = new StringBuilder(imageBaseName);
                sb.append(' ');
                sb.append(newMajorVersion);
                sb.append('.');
                sb.append(newMinorVersion);
                if (newMicroVersion != null) {
                    sb.append('.');
                    sb.append(newMicroVersion);
                }
                result = sb.toString();
            } else {
                result = imageName+" (1)";
            }
            return result;
        }

        @Override
        public BuilderT setUpgradedImageName(String upgradedImageName) {
            this.upgradedImageName = upgradedImageName;
            return self();
        }

        @Override
        public BuilderT setSnapshotBaseName(String deviceName, String snapshotBaseName) {
            deviceNamesToSnapshotBaseNames.put(deviceName, snapshotBaseName);
            return self();
        }

        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return SailingAnalyticsHostImpl::new;
        }

        protected String getUpgradedImageName() {
            return upgradedImageName;
        }
        
        protected Map<String, String> getDeviceNamesToSnapshotBaseNames() {
            return Collections.unmodifiableMap(deviceNamesToSnapshotBaseNames);
        }

        @Override
        public UpgradeAmi<ShardingKey, MasterProcessT, ReplicaProcessT> build() {
            if (upgradedImageName == null) {
                upgradedImageName = increaseVersionNumber(getMachineImage().getName());
            }
            return new UpgradeAmi<>(this);
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey, MasterProcessT, ReplicaProcessT>, ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> Builder<BuilderT, ShardingKey, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }
    
    protected UpgradeAmi(BuilderImpl<?, ShardingKey, MasterProcessT, ReplicaProcessT> builder) {
        super(builder);
        upgradedImageName = builder.getUpgradedImageName();
        timeout = builder.getOptionalTimeout().orElse(null);
        waitForShutdown = !builder.isNoShutdown();
        deviceNamesToSnapshotBaseNames = builder.getDeviceNamesToSnapshotBaseNames();
    }

    @Override
    public void run() throws Exception {
        try {
            super.run(); // launches the machine in upgrade mode and shuts it down again, preparing for AMI creation
            Instance instance = getLandscape().getInstance(getHost().getInstanceId(), getHost().getRegion());
            if (waitForShutdown) {
                logger.info("Waiting for shutdown of instance "+instance.instanceId());
                // wait for the instance to shut down
                final TimePoint startedWaiting = TimePoint.now();
                while (instance.state().name() != InstanceStateName.STOPPED && (timeout == null || startedWaiting.until(TimePoint.now()).compareTo(timeout) < 0)) {
                    logger.info("Instance " + instance.instanceId() + " still in state " + instance.state().name()
                            + ". Waiting " + (timeout == null ? "forever"
                                    : ("for another " + timeout.minus(startedWaiting.until(TimePoint.now())))));
                    Thread.sleep(5000);
                    instance = getLandscape().getInstance(getHost().getInstanceId(), getHost().getRegion());
                }
            }
            upgradedAmi = getLandscape().createImage(getHost(), upgradedImageName);
            final TimePoint startedWaiting = TimePoint.now();
            while ((upgradedAmi=getLandscape().getImage(upgradedAmi.getRegion(), upgradedAmi.getId())).getState() != ImageState.AVAILABLE && (timeout == null || startedWaiting.until(TimePoint.now()).compareTo(timeout) < 0)) {
                logger.info("Image " + upgradedAmi.getId() + " still in state " + upgradedAmi.getState()
                        + ". Waiting " + (timeout == null ? "forever"
                                : ("for another " + timeout.minus(startedWaiting.until(TimePoint.now())))));
                Thread.sleep(5000);
            }
            for (final BlockDeviceMapping blockDeviceMapping : upgradedAmi.getBlockDeviceMappings()) {
                if (blockDeviceMapping.ebs() != null) {
                    final String snapshotId = blockDeviceMapping.ebs().snapshotId();
                    final String deviceName = blockDeviceMapping.deviceName();
                    final String snapshotName = getSnapshotName(deviceName);
                    getLandscape().setSnapshotName(getHost().getRegion(), snapshotId, snapshotName);
                }
            }
        } finally {
            if (getHost() != null) {
                getHost().terminate();
            }
        }
    }
    
    private String getSnapshotName(String deviceName) {
        final StringBuilder result = new StringBuilder();
        result.append(upgradedImageName);
        final String baseName = deviceNamesToSnapshotBaseNames.get(deviceName);
        if (baseName != null) {
            result.append(" (");
            result.append(baseName);
            result.append(")");
        }
        return result.toString();
    }

    /**
     * @return the resulting AMI that has the upgraded version of everything
     */
    public AmazonMachineImage<ShardingKey, SailingAnalyticsMetrics> getUpgradedAmi() {
        return upgradedAmi;
    }
}
