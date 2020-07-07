package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Region;

public class AwsAvailabilityZone implements AvailabilityZone {
    private final String id;
    private final Region region;
    
    public AwsAvailabilityZone(String id, Region region) {
        super();
        this.id = id;
        this.region = region;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Region getRegion() {
        return region;
    }

}
