package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class SSHKeyPairDTO implements IsSerializable {
    private String regionId;
    private String name;
    private String creatorName;
    private TimePoint creationTime;

    @Deprecated
    SSHKeyPairDTO() {
        // for GWT RPC serialization only
    }

    public SSHKeyPairDTO(String regionId, String name, String creatorName, TimePoint creationTime) {
        super();
        this.regionId = regionId;
        this.name = name;
        this.creatorName = creatorName;
        this.creationTime = creationTime;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getName() {
        return name;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public TimePoint getCreationTime() {
        return creationTime;
    }
}
