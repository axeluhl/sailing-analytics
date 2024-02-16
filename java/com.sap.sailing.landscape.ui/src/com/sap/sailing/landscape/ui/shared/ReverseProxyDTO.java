package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.TimePoint;

public class ReverseProxyDTO extends AwsInstanceDTO  {

    @Deprecated
    ReverseProxyDTO() {} 
    
    private String name;
    private String amiId;
    private String health;
    private boolean isDisposable = false;

    public ReverseProxyDTO(String instanceId, String privateIpAddress, String publicIpAddress,
            String region, TimePoint launchTimePoint, boolean shared, String name, String imageId, String healthInTargetGroup,
            boolean isDisposable, AvailabilityZoneDTO availabilityZoneDTO) {
        super(instanceId, privateIpAddress, publicIpAddress, region, launchTimePoint, shared, availabilityZoneDTO);
        this.name = name;
        this.amiId = imageId;
        this.health = healthInTargetGroup;
        this.isDisposable = isDisposable;
    }
    
    public String getName() {
        return name;
    }

    public String getImageId() {
        return amiId;
    }

    public String getHealth() {
        return health;
    }

    public boolean isDisposable() {
        return isDisposable;
    }
}
