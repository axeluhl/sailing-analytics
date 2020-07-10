package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;

public class AwsAvailabilityZoneImpl implements AwsAvailabilityZone {
    private final String id;
    private final Region region;
    
    public AwsAvailabilityZoneImpl(String id, Region region) {
        super();
        this.id = id;
        this.region = region;
    }
    
    public AwsAvailabilityZoneImpl(software.amazon.awssdk.services.ec2.model.AvailabilityZone az) {
        this(az.zoneName(), new AwsRegion(az.regionName()));
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
