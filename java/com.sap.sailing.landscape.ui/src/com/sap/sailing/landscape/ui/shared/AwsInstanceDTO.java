package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class AwsInstanceDTO implements IsSerializable {
    private String instanceId;
    private String availabilityZone;
    private String privateIpAddress;
    private String publicIpAddress;
    private String region;
    private TimePoint launchTimePoint;
    
    @Deprecated
    AwsInstanceDTO() {} // for GWT RPC serialization only
    
    public AwsInstanceDTO(String instanceId, String availabilityZone, String privateIpAddress, String publicIpAddress, String region, TimePoint launchTimePoint) {
        super();
        this.instanceId = instanceId;
        this.availabilityZone = availabilityZone;
        this.privateIpAddress = privateIpAddress;
        this.publicIpAddress = publicIpAddress;
        this.region = region;
        this.launchTimePoint = launchTimePoint;
    }
    public String getInstanceId() {
        return instanceId;
    }
    public String getAvailabilityZone() {
        return availabilityZone;
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
}
