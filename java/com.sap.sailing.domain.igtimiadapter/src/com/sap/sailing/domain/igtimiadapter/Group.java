package com.sap.sailing.domain.igtimiadapter;

public interface Group extends SecurityEntity {
    String getName();

    Boolean isHidden();

    Iterable<Permission> getPermissions();
    
    Boolean isBlob();
}
