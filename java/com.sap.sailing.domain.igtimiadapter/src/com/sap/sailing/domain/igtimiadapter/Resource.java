package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.datatypes.Type;


public interface Resource extends HasId, HasPermissions, HasStartAndEndTime {

    boolean isBlob();

    Iterable<Type> getDataTypes();

    String getDeviceSerialNumber();

}
