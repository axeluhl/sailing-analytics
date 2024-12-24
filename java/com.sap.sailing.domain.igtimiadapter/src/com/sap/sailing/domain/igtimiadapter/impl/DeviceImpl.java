package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class DeviceImpl extends HasIdImpl implements Device {
    private static final long serialVersionUID = 7224992550721569935L;
    private final String serialNumber;
    private final String name;
    private String serviceTag;
    
    public DeviceImpl(long id, String serialNumber) {
        this(id, serialNumber, /* name */ null, /* serviceTag */ null);
    }

    public DeviceImpl(Long id, String serialNumber, String name, String serviceTag) {
        super(id);
        this.serialNumber = serialNumber;
        this.name = name;
        this.serviceTag = serviceTag;
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
