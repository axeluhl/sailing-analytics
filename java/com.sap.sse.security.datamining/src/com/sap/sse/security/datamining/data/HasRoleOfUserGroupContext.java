package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasRoleOfUserGroupContext {
    @Connector(scanForStatistics = false)
    HasUserGroupContext getUserGroupContext();

    @Dimension(messageKey = "RoleOfUserGroupName")
    String getRoleName();

    @Dimension(messageKey = "RoleOfUserGroupForAll")
    boolean isForAll();

    @Statistic(messageKey = "NumberOfPermissions")
    int getNumberOfPermissions();
}