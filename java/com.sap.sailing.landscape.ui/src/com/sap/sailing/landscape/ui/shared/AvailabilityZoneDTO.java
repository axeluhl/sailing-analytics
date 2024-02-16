package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AvailabilityZoneDTO  implements IsSerializable {
    private String azName;
    private String region; 
    private String azId;
    @Deprecated
    AvailabilityZoneDTO() {} // GWT serialisation only

    public AvailabilityZoneDTO(String azName, String region, String azId) {
        this.azName = azName;
        this.region = region;
        this.azId = azId;
    } 

    public String getAzName() {
        return azName;
    }

    public String getRegion() {
        return region;
    }

    public String getAzId() {
        return azId;
    }
}
