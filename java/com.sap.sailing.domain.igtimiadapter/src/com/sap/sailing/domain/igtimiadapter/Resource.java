package com.sap.sailing.domain.igtimiadapter;


public interface Resource extends HasId, HasPermissions, HasStartAndEndTime {

    boolean isBlob();

    int[] getDataTypes();

    String getDeviceSerialNumber();

}
