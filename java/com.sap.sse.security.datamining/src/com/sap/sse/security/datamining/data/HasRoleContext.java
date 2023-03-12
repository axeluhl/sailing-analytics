package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.shared.impl.Role;

public interface HasRoleContext {
    @Dimension(messageKey = "RoleName")
    default String getRoleName() {
        return getRole().getName();
    }
    
    @Dimension(messageKey = "IsGroupQualified")
    default boolean isGroupQualified() {
        return getRole().getQualifiedForTenant() != null;
    }

    @Dimension(messageKey = "IsUserQualified")
    default boolean isUserQualified() {
        return getRole().getQualifiedForUser() != null;
    }

    @Statistic(messageKey = "NumberOfPermissions")
    default int getNumberOfPermissions() {
        return getRole().getPermissions().size();
    }
    
    Role getRole();
}
