package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class AmazonMachineImageDTO implements IsSerializable {
    private String id;
    private String regionId;
    private String name;
    private String type;
    private String state;
    private TimePoint creationTimePoint;
    
    @Deprecated
    AmazonMachineImageDTO() {} // for GWT de-serialization only

    public AmazonMachineImageDTO(String id, String regionId, String name, String type, String state, TimePoint creationTimePoint) {
        super();
        this.id = id;
        this.regionId = regionId;
        this.name = name;
        this.type = type;
        this.state = state;
        this.creationTimePoint = creationTimePoint;
    }

    public String getId() {
        return id;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public TimePoint getCreationTimePoint() {
        return creationTimePoint;
    }

    @Override
    public String toString() {
        return "AmazonMachineImageDTO [id=" + id + ", regionId=" + regionId + ", name=" + name + ", type=" + type
                + ", state=" + state + ", creationTimePoint=" + creationTimePoint + "]";
    }
}
