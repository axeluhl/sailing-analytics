package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class ResourceImpl implements Resource {
    private static final long serialVersionUID = -8944469021862963744L;
    private final long id;
    private final TimePoint startTime;
    private final TimePoint endTime;
    private final String deviceSerialNumber;
    private final int[] dataTypes;
    
    public ResourceImpl(long id, TimePoint startTime, TimePoint endTime, String deviceSerialNumber, int[] dataTypes) {
        super();
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deviceSerialNumber = deviceSerialNumber;
        this.dataTypes = dataTypes;
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
    public Iterable<Type> getDataTypes() {
        List<Type> result = new ArrayList<Type>();
        for (int dataType : dataTypes) {
            result.add(Type.valueOf(dataType));
        }
        return result;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(new TypeRelativeObjectIdentifier(""+getId()));
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.IGTIMI_RESOURCE;
    }

    @Override
    public String getName() {
        return "Igtimi Resource "+getId();
    }
}
