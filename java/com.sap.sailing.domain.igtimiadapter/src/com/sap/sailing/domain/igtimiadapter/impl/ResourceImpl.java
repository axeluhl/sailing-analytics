package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;

public class ResourceImpl implements Resource {
    private final long id;
    private final TimePoint startTime;
    private final TimePoint endTime;
    private final String deviceSerialNumber;
    private final int[] dataTypes;
    private final Iterable<Permission> permissions;
    private final boolean blob;
    
    public ResourceImpl(long id, TimePoint startTime, TimePoint endTime, String deviceSerialNumber, int[] dataTypes,
            Iterable<Permission> permissions, boolean blob) {
        super();
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deviceSerialNumber = deviceSerialNumber;
        this.dataTypes = dataTypes;
        this.permissions = permissions;
        this.blob = blob;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TimePoint getStartTime() {
        return startTime;
    }

    @Override
    public TimePoint getEndTime() {
        return endTime;
    }

    @Override
    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    @Override
    public int[] getDataTypes() {
        return dataTypes;
    }

    @Override
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isBlob() {
        return blob;
    }
}
