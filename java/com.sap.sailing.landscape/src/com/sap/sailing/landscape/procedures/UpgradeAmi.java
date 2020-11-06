package com.sap.sailing.landscape.procedures;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.impl.SailingAnalyticsHostImpl;
import com.sap.sailing.landscape.procedures.UpgradeAmi.Builder;
import com.sap.sailing.landscape.procedures.UpgradeAmi.Builder.VersionPart;
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
extends StartEmptyServer<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT> {
    private static final String IMAGE_UPGRADE_USER_DATA = "image-upgrade";
    private static final String NO_SHUTDOWN_USER_DATA = "no-shutdown";
    private static final Pattern imageNamePattern = Pattern.compile("^(.*) ([0-9]+)\\.([0-9]+)(\\.([0-9]+))?$");
    
    private final String upgradedImageName;
    private MachineImage upgradedAmi;
    
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
    public static interface Builder<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
            extends
            StartEmptyServer.Builder<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> {
        enum VersionPart {
            MAJOR, MINOR, MICRO
        }

        Builder<ShardingKey, MasterProcessT, ReplicaProcessT> setUpgradedImageName(String upgradedImageName);

        Builder<ShardingKey, MasterProcessT, ReplicaProcessT> setVersionPartToIncrement(VersionPart versionPartToIncrement);
    }

    protected static class BuilderImpl<ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>>
    extends StartEmptyServer.BuilderImpl<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>>
    implements Builder<ShardingKey, MasterProcessT, ReplicaProcessT> {
        private String upgradedImageName;
        private VersionPart versionPartToIncrement;

        private BuilderImpl() {
            super();
            setNoShutdown(false);
        }
        
        @Override
        public Builder<ShardingKey, MasterProcessT, ReplicaProcessT> setVersionPartToIncrement(VersionPart versionPartToIncrement) {
            this.versionPartToIncrement = versionPartToIncrement;
            return this;
        }

        private String increaseVersionNumber(String imageName) {
            final String result;
            final Matcher versionNumberMatcher = imageNamePattern.matcher(imageName);
            if (versionNumberMatcher.matches()) {
                final String imageBaseName = versionNumberMatcher.group(1);
                final String majorVersion = versionNumberMatcher.group(2);
                final String minorVersion = versionNumberMatcher.group(3);
                final String microVersion = versionNumberMatcher.group(5);
                final StringBuilder sb = new StringBuilder(imageBaseName);
                sb.append(' ');
                sb.append(majorVersion);
                sb.append('.');
                sb.append(minorVersion);
                if (microVersion != null || versionPartToIncrement == VersionPart.MICRO) {
                    sb.append('.');
                    sb.append(microVersion);
                }
                result = sb.toString();
            } else {
                result = imageName+" (1)";
            }
            return result;
        }

        @Override
        public Builder<ShardingKey, MasterProcessT, ReplicaProcessT> setUpgradedImageName(String upgradedImageName) {
            this.upgradedImageName = upgradedImageName;
            return this;
        }

        @Override
        public HostSupplier<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT, SailingAnalyticsHost<ShardingKey>> getHostSupplier() {
            return SailingAnalyticsHostImpl::new;
        }

        protected String getUpgradedImageName() {
            return upgradedImageName;
        }

        @Override
        public UpgradeAmi<ShardingKey, MasterProcessT, ReplicaProcessT> build() {
            if (upgradedImageName == null) {
                upgradedImageName = increaseVersionNumber(getMachineImage().getName());
            }
            return new UpgradeAmi<>(this);
        }
    }
    
    public static <ShardingKey,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, SailingAnalyticsMetrics, MasterProcessT, ReplicaProcessT>,
    HostT extends AwsInstance<ShardingKey, SailingAnalyticsMetrics>> Builder<ShardingKey, MasterProcessT, ReplicaProcessT> builder() {
        return new BuilderImpl<>();
    }
    
    protected UpgradeAmi(BuilderImpl<ShardingKey, MasterProcessT, ReplicaProcessT> builder) {
        super(builder);
        upgradedImageName = builder.getUpgradedImageName();
        addUserData(Collections.singleton(IMAGE_UPGRADE_USER_DATA));
        if (builder.isNoShutdown()) {
            addUserData(Collections.singleton(NO_SHUTDOWN_USER_DATA));
        }
    }

    @Override
    public void run() throws Exception {
        super.run(); // launches the machine in upgrade mode and shuts it down again, preparing for AMI creation
        upgradedAmi = getLandscape().createImage(getHost(), upgradedImageName);
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
