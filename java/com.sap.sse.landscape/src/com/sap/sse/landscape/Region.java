package com.sap.sse.landscape;

public interface Region {
    Iterable<AvailabilityZone> getAvailabilityZones();
    Iterable<SecurityGroup> getSecurityGroups();
}
