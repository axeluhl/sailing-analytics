package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class AwsInstanceDTO implements IsSerializable {
    private String instanceId;
    private String availabilityZoneName;
    private String privateIpAddress;
    private String publicIpAddress;
    private String region;
    private TimePoint launchTimePoint;
    private boolean shared;
    private String availabilityZoneId;
    
    @Deprecated
    AwsInstanceDTO() {} // for GWT RPC serialization only
    
    public AwsInstanceDTO(String instanceId, String availabilityZoneId, String privateIpAddress, String publicIpAddress, String region, TimePoint launchTimePoint, boolean shared, String availabilityZoneName) {
        super();
        this.instanceId = instanceId;
        this.availabilityZoneName = availabilityZoneName;
        this.privateIpAddress = privateIpAddress;
        this.availabilityZoneId = availabilityZoneId;
        this.publicIpAddress = publicIpAddress;
        this.region = region;
        this.launchTimePoint = launchTimePoint;
        this.shared = shared;
    }
    public String getAvailabilityZoneId() {
        return availabilityZoneId;
    }
    public String getInstanceId() {
        return instanceId;
    }
    public String getAvailabilityZoneName() {
        return availabilityZoneName;
    }
    public String getRegion() {
        return region;
    }
    public TimePoint getLaunchTimePoint() {
        return launchTimePoint;
    }
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }
    public String getPublicIpAddress() {
        return publicIpAddress;
    }
    public boolean isShared() {
        return shared;
    }
}
