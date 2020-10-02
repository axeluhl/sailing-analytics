package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsLandscape;

public class AwsRegion implements Region {
    private AwsLandscape<?, ?, ?, ?> landscape;
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

    @Override
    public String toString() {
        return "AwsRegion [id=" + id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AwsRegion other = (AwsRegion) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
