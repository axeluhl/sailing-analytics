package com.sap.sse.landscape.aws.impl;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;

public class AwsAvailabilityZoneImpl extends NamedImpl implements AwsAvailabilityZone {
    private static final long serialVersionUID = 7081008193504850891L;
    private final String id;
    private final AwsRegion region;
    
    public AwsAvailabilityZoneImpl(String id, String name, AwsRegion region) {
        super(name);
        this.id = id;
        this.region = region;
    }
    
    public AwsAvailabilityZoneImpl(software.amazon.awssdk.services.ec2.model.AvailabilityZone az) {
        this(az.zoneId(), az.zoneName(), new AwsRegion(az.regionName()));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AwsRegion getRegion() {
        return region;
    }

}
