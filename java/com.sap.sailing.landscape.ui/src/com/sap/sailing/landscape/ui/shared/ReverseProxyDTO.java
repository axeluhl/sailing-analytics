package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.TimePoint;

public class ReverseProxyDTO extends AwsInstanceDTO  {

    @Deprecated
    ReverseProxyDTO() {} 
    
    String name;
    String amiId;
    String health;
    boolean isDisposable = false;

    public ReverseProxyDTO(String instanceId, String availabilityZoneName, String privateIpAddress,
            String availabilityZoneId, String publicIpAddress, String region, TimePoint launchTimePoint, boolean shared, String name,
            String imageId, String healthInTargetGroup) {
        super(instanceId, availabilityZoneName, privateIpAddress, availabilityZoneId, publicIpAddress, region, launchTimePoint, shared);
        this.name = name;
        this.amiId = imageId;
        this.health=healthInTargetGroup;
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


    public void setDisposable(boolean isDisposable) {
        this.isDisposable= isDisposable;
        
    }
    
    public boolean isDisposable() {
        return isDisposable;
    }
    
    
    
    
    
}
