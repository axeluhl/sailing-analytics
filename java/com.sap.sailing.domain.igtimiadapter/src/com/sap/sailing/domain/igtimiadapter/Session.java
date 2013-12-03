package com.sap.sailing.domain.igtimiadapter;

public interface Session extends HasId, HasPermissions, HasStartAndEndTime {
    boolean isBlob();

    long getAdminSessionGroupId();

    long getSessionGroupId();

    long getOwnerId();

    String getName();
}
