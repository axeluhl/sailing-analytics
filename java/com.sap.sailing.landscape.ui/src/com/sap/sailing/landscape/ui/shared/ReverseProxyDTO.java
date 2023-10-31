package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.TimePoint;

public class ReverseProxyDTO extends AwsInstanceDTO  {

    @Deprecated
    ReverseProxyDTO() {} 
    
    String name;
    public ReverseProxyDTO(String instanceId, String availabilityZoneId, String privateIpAddress, String publicIpAddress, String region, TimePoint launchTimePoint, boolean shared,String name) {
        super( instanceId,  availabilityZoneId,  privateIpAddress,  publicIpAddress,  region,  launchTimePoint,  shared);
        this.name=name;
    }
    
    
    public String getName() {
        return name;
    }
}
