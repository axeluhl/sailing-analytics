package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsLandscape;

public class AwsRegion implements Region {
    private AwsLandscape<?, ?> landscape;
    private final String id;
    
    public AwsRegion(String id) {
        this.id = id;
    }
    
    public AwsRegion(software.amazon.awssdk.regions.Region region) {
        this(region.id());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Iterable<AvailabilityZone> getAvailabilityZones() {
        return landscape.getAvailabilityZones(this);
    }

    @Override
    public Iterable<SecurityGroup> getSecurityGroups() {
        // TODO Implement AwsRegion.getSecurityGroups(...)
        return null;
    }

}
