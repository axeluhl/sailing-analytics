package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class DeviceImpl extends HasIdImpl implements Device {
    private static final long serialVersionUID = 7224992550721569935L;
    private final String serialNumber;
    private final String name;
    private String serviceTag;
    private Iterable<Permission> permissions;
    
    protected DeviceImpl(long id, String serialNumber, IgtimiConnection conn) {
        this(id, serialNumber, /* name */ null, /* serviceTag */ null, /* permissions */ null);
    }

    public DeviceImpl(Long id, String serialNumber, String name, String serviceTag, Iterable<Permission> permissions) {
        super(id);
        this.serialNumber = serialNumber;
        this.name = name;
        this.serviceTag = serialNumber;
        this.permissions = permissions;
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
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(new TypeRelativeObjectIdentifier(getSerialNumber()));
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.IGTIMI_DEVICE;
    }
}
