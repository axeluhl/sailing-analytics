package com.sap.sse.landscape.aws.orchestration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.orchestration.StartHost;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Launches an AWS host when {@link #run() run}. The resulting {@link AwsInstance} can then be obtained by
 * calling {@link #getHost()}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 * @param <MetricsT>
 * @param <MasterProcessT>
 * @param <ReplicaProcessT>
 * @param <HostT>
 */
public abstract class StartAwsHost<ShardingKey,
                          MetricsT extends ApplicationProcessMetrics,
                          MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
    private static final Logger logger = Logger.getLogger(StartAwsHost.class.getName());
    
    /**
     * The {@link AwsLandscape#getLatestImageWithTag(com.sap.sse.landscape.Region, String, String)} method is
     * used to obtain default images for specific AWS host starting procedures that subclass this class. The
     * Amazon Machine Images (AMIs) for this are then expected to be tagged with a tag named as specified by this
     * constant ("image-type"). The tag value then must match what the subclass wants.
     * 
     * @see #getLatestImageOfType(String)
     */
    private final static String IMAGE_TYPE_TAG_NAME = "image-type";

    private final List<String> userData;
    private final InstanceType instanceType;
    private final AwsAvailabilityZone availabilityZone;
    private final String keyName;
    private final Iterable<SecurityGroup> securityGroups;
    private final Optional<Tags> tags;
    private AwsInstance<ShardingKey, MetricsT> host;
    
    public StartAwsHost(MachineImage machineImage, Optional<Release> release,
            AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape,
            InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Iterable<SecurityGroup> securityGroups, Optional<Tags> tags, String... userData) {
        super(machineImage, landscape);
        this.userData = new ArrayList<>();
        for (final String ud : userData) {
            this.userData.add(ud);
        }
        this.instanceType = instanceType;
        this.availabilityZone = availabilityZone;
        this.keyName = keyName;
        this.tags = tags;
        this.securityGroups = securityGroups;
        release.ifPresent(this::addUserData);
    }
    
    public StartAwsHost(AmazonMachineImage<ShardingKey, MetricsT> machineImage,
            Optional<Release> release,
            AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, InstanceType instanceType, AwsAvailabilityZone availabilityZone, String keyName,
            Optional<Tags> tags, String[] userData) {
        this(machineImage, release, landscape, instanceType, availabilityZone,
                keyName,
                Collections.singleton(landscape.getDefaultSecurityGroupForApplicationHosts(availabilityZone.getRegion())), tags, userData);
    }

    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    AmazonMachineImage<ShardingKey, MetricsT> getLatestImageOfType(String imageType, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, Region region) {
        return landscape.getLatestImageWithTag(region, IMAGE_TYPE_TAG_NAME, imageType);
    }
    
    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    AwsAvailabilityZone getRandomAvailabilityZone(AwsRegion region, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        final Iterable<AvailabilityZone> azs = landscape.getAvailabilityZones(region);
        return (AwsAvailabilityZone) Util.get(azs, new Random().nextInt(Util.size(azs)));
    }

    protected static <ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
    Set<SecurityGroup> getDefaultSecurityGroupForApplicationHosts(Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, Region region) {
        return Collections.singleton(landscape.getDefaultSecurityGroupForApplicationHosts(region));
    }

    protected static Optional<Tags> addNameTag(String name, Optional<Tags> tags) {
        return Optional.of(tags.orElse(Tags.empty()).and("Name", name));
    }

    @Override
    public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
    }

    @Override
    public void run() {
        try {
            host = getLandscape().launchHost(getMachineImage(), getInstanceType(), getAvailabilityZone(), getKeyName(), getSecurityGroups(), getTags(),
                    Util.toArray(getUserData(), new String[0]));
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Exception trying to launch host", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @return {@code null} before {@link #run()} is called; the host launched afterwards
     */
    public AwsInstance<ShardingKey, MetricsT> getHost() {
        return host;
    }

    public void setHost(AwsInstance<ShardingKey, MetricsT> host) {
        this.host = host;
    }

    private Optional<Tags> getTags() {
        return tags;
    }

    private Iterable<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    private String getKeyName() {
        return keyName;
    }

    private AwsAvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    private InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * @return the user data to pass to EC2 when launching the instance; based on the {@link #userData} passed to this
     *         object's constructor plus extensions added by calling {@link #addUserData}. Subclasses may override this
     *         method to change or add to these user data.
     */
    protected Iterable<String> getUserData() throws URISyntaxException {
        return userData;
    }
    
    /**
     * Appends {@code moreUserData} to the end of {@link #userData}
     */
    protected void addUserData(Iterable<String> moreUserData) {
        Util.addAll(moreUserData, userData);
    }
    
    protected void addUserData(ProcessConfigurationVariable userDataVariable, String value) {
        userData.add(userDataVariable.name()+"="+value);
    }

    /**
     * Appends {@code moreUserData} to the end of {@link #userData}
     */
    protected void addUserData(UserDataProvider userDataProvider) {
        for (final Entry<ProcessConfigurationVariable, String> userDataEntry : userDataProvider.getUserData().entrySet()) {
            addUserData(userDataEntry.getKey(), userDataEntry.getValue());
        }
    }
}
