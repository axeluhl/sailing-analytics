package com.sap.sailing.domain.igtimiadapter;

public interface Group extends SecurityEntity {
    String getName();

    boolean isHidden();

    Iterable<Permission> getPermissions();
    
    boolean isBlob();
}
