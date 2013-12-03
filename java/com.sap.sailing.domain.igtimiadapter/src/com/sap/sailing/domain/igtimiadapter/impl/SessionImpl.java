package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Session;

public class SessionImpl extends HasIdImpl implements Session {
    private final long ownerId;
    private final String name;
    private final long sessionGroupId;
    private final long adminSessionGroupId;
    private final Iterable<Permission> permissions;
    private final TimePoint startTime;
    private final TimePoint endTime;
    private final boolean blob;

    protected SessionImpl(long id, String name, long ownerId, long sessionGroupId,
            long adminSessionGroupId, Iterable<Permission> permissions, TimePoint startTime, TimePoint endTime, boolean blob) {
        super(id);
        this.name = name;
        this.ownerId = ownerId;
        this.sessionGroupId = sessionGroupId;
        this.adminSessionGroupId = adminSessionGroupId;
        this.permissions = permissions;
        this.startTime = startTime;
        this.endTime = endTime;
        this.blob = blob;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public long getSessionGroupId() {
        return sessionGroupId;
    }

    @Override
    public long getAdminSessionGroupId() {
        return adminSessionGroupId;
    }

    @Override
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public TimePoint getEndTime() {
        return endTime;
    }

    @Override
    public TimePoint getStartTime() {
        return startTime;
    }

    @Override
    public boolean isBlob() {
        return blob;
    }

}
