package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.common.TimePoint;

public interface Resource {

    long getId();

    public abstract boolean isBlob();

    public abstract Iterable<Permission> getPermissions();

    public abstract int[] getDataTypes();

    public abstract String getDeviceSerialNumber();

    public abstract TimePoint getEndTime();

    public abstract TimePoint getStartTime();

}
