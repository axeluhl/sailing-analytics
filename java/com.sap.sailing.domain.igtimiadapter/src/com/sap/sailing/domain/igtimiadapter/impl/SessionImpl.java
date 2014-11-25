package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Session;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sse.common.TimePoint;

public class SessionImpl extends HasIdImpl implements Session {
    private final long ownerId;
    private final String name;
    private final long sessionGroupId;
    private final long adminSessionGroupId;
    private final Iterable<Permission> permissions;
    private final TimePoint startTime;
    private final TimePoint endTime;
    private final boolean blob;
    private final IgtimiConnection conn;

    protected SessionImpl(long id, String name, long ownerId, long sessionGroupId,
            long adminSessionGroupId, Iterable<Permission> permissions, TimePoint startTime, TimePoint endTime, boolean blob, IgtimiConnection conn) {
        super(id);
        this.name = name;
        this.ownerId = ownerId;
        this.sessionGroupId = sessionGroupId;
        this.adminSessionGroupId = adminSessionGroupId;
        this.permissions = permissions;
        this.startTime = startTime;
        this.endTime = endTime;
        this.blob = blob;
        this.conn = conn;
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

    @Override
    public User getOwner() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        return conn.getUser(getOwnerId());
    }
}
