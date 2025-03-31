package com.sap.sse.security.datamining.data;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.security.shared.WildcardPermission;

public interface HasPermissionContext {
    WildcardPermission getPermission();
    
    @Dimension(messageKey="PermissionString", ordinal=0)
    default String getPermissionString() {
        return getPermission().toString();
    }
    
    @Dimension(messageKey="PermissionTypes", ordinal=1)
    default String getPermissionTypes() {
        return !getPermission().getParts().isEmpty() ? Util.joinStrings(",", getPermission().getParts().get(0)) : "";
    }
    
    @Dimension(messageKey="PermissionActions", ordinal=2)
    default String getPermissionActions() {
        return getPermission().getParts().size() > 1 ? Util.joinStrings(",", getPermission().getParts().get(1)) : "";
    }
    
    @Dimension(messageKey="PermissionObjects", ordinal=3)
    default String getPermissionObjects() {
        return getPermission().getParts().size() > 2 ? Util.joinStrings(",", getPermission().getParts().get(2)) : "";
    }
}
