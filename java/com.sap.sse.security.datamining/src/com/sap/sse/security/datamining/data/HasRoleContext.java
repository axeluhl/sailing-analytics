package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.shared.impl.Role;

public interface HasRoleContext {
    @Dimension(messageKey = "RoleName")
    default String getRoleName() {
        return getRole().getName();
    }

    @Statistic(messageKey = "NumberOfPermissions")
    default int getNumberOfPermissions() {
        return getRole().getPermissions().size();
    }
    
    Role getRole();
}
