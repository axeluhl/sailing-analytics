package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.User;

public class DeviceImpl extends HasIdImpl implements Device {
    private final String serialNumber;
    private final String name;
    private String serviceTag;
    private Long ownerId;
    private Long deviceUserGroupId;
    private Long adminDeviceUserGroupId;
    private Iterable<Permission> permissions;
    private Boolean blob;
    private final IgtimiConnection conn;
    
    protected DeviceImpl(long id, String serialNumber, IgtimiConnection conn) {
        this(id, serialNumber, /* name */ null, /* serviceTag */ null, /* ownerId */ null, /* deviceUserGroupId */ null,
                /* adminDeviceUserGroupId */ null, /* permissions */ null, /* blob */ null, conn);
    }

    public DeviceImpl(Long id, String serialNumber, String name, String serviceTag, Long ownerId, Long deviceUserGroupId,
            Long adminDeviceUserGroupId, Iterable<Permission> permissions, Boolean blob, IgtimiConnection conn) {
        super(id);
        this.serialNumber = serialNumber;
        this.name = name;
        this.serviceTag = serialNumber;
        this.ownerId = ownerId;
        this.deviceUserGroupId = deviceUserGroupId;
        this.adminDeviceUserGroupId = adminDeviceUserGroupId;
        this.permissions = permissions;
        this.blob = blob;
        this.conn = conn;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getServiceTag() {
        return serviceTag;
    }

    @Override
    public Long getOwnerId() {
        return ownerId;
    }

    @Override
    public Long getDeviceUserGroupId() {
        return deviceUserGroupId;
    }
    
    @Override
    public Long getAdminDeviceUserGroupId() {
        return adminDeviceUserGroupId;
    }
    
    @Override
    public User getOwner() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        return conn.getUser(getOwnerId());
    }
    
    @Override
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public Boolean getBlob() {
        return blob;
    }

    @Override
    public String getName() {
        return name;
    }

}
