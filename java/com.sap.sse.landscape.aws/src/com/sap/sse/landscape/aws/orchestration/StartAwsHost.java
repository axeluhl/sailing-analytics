package com.sap.sse.landscape.aws.orchestration;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.orchestration.StartHost;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public abstract class StartAwsHost<ShardingKey,
                          MetricsT extends ApplicationProcessMetrics,
                          MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                          HostT extends AwsInstance<ShardingKey, MetricsT>>
extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> {
    private static final Logger logger = Logger.getLogger(StartAwsHost.class.getName());
    private final String[] userData;
    private final InstanceType instanceType;
    private final AwsAvailabilityZone availabilityZone;
    private final String keyName;
    private final Iterable<SecurityGroup> securityGroups;
    private final Optional<Tags> tags;
    
    public StartAwsHost(MachineImage<HostT> machineImage, Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape,
            InstanceType instanceType,
            AwsAvailabilityZone availabilityZone, String keyName, Iterable<SecurityGroup> securityGroups,
            Optional<Tags> tags, String... userData) {
        super(machineImage, landscape);
        this.userData = userData;
        this.instanceType = instanceType;
        this.availabilityZone = availabilityZone;
        this.keyName = keyName;
        this.tags = tags;
        this.securityGroups = securityGroups;
    }
    
    @Override
    public AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return (AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>) super.getLandscape();
    }

    @Override
    public void run() {
        try {
            getLandscape().launchHost(getMachineImage(), getInstanceType(), getAvailabilityZone(), getKeyName(), getSecurityGroups(), getTags(), getUserData());
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Exception trying to launch host", e);
            throw new RuntimeException(e);
        }
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
     *         object's constructor. Subclasses may override this method to change or add to these user data.
     */
    protected String[] getUserData() throws URISyntaxException {
        return userData;
    }
    
    /**
     * Joins the result of {@link #getUserData()} with the {@code additionalUserData} passed to this method. The result
     * is a new array with the additional user data following the {@link #getUserData()} from this object.
     */
    protected String[] joinUserData(Iterable<String> additionalUserData) {
        final String[] result = new String[userData.length + Util.size(additionalUserData)];
        System.arraycopy(userData, 0, result, 0, userData.length);
        int i=userData.length;
        for (final String additionalUserDataElement : additionalUserData) {
            result[i++] = additionalUserDataElement;
        }
        return result;
    }
}
