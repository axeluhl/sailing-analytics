package com.sap.sse.security.shared;

import java.util.Set;

import com.sap.sse.common.WithID;

public interface Role extends WithID {
    String getDisplayName();
    Set<String> getPermissions();
}
