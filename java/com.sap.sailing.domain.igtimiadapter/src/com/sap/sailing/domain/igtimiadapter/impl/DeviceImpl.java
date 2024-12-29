package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class DeviceImpl extends HasIdImpl implements Device {
    private static final long serialVersionUID = 7224992550721569935L;
    private final String serialNumber;
    private String name;
    
    public DeviceImpl(long id, String serialNumber) {
        this(id, serialNumber, /* name */ null);
    }

    public DeviceImpl(Long id, String serialNumber, String name) {
        super(id);
        this.serialNumber = serialNumber;
        this.name = name;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
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
