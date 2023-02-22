package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasPermissionOfUserInUserGroupContext extends HasPermissionContext {
    @Connector(messageKey = "UsersInUserGroup", scanForStatistics = false)
    HasUserInUserGroupContext getUserInUserGroupContext();
}
