package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.SecurityEntity;
import com.sap.sse.common.TimePoint;

public class DataAccessWindowImpl extends HasIdImpl implements DataAccessWindow {
    private final TimePoint startTime;
    private final TimePoint endTime;
    private final String deviceSerialNumber;
    private final Iterable<Permission> permissions;
    private final SecurityEntity recipient;

    public DataAccessWindowImpl(long id, TimePoint startTime, TimePoint endTime, String deviceSerialNumber,
            Iterable<Permission> permissions, SecurityEntity recipient) {
        super(id);
        this.startTime = startTime;
        this.endTime = endTime;
        this.deviceSerialNumber = deviceSerialNumber;
        this.permissions = permissions;
        this.recipient = recipient;
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
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public SecurityEntity getRecipient() {
        return recipient;
    }
    
    @Override
    public String toString() {
        return "DAW "+getId()+" for device "+getDeviceSerialNumber()+" from "+getStartTime()+" to "+getEndTime()+", permissions "+getPermissions();
    }
}
