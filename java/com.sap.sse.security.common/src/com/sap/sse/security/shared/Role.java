package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public interface Role extends NamedWithID {
    Set<WildcardPermission> getPermissions();
    
    @Override
    UUID getId();
}
