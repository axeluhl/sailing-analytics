package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasPermissionOfUserContext extends HasPermissionContext {
    @Connector(messageKey = "User", scanForStatistics = false)
    HasUserContext getUser();
}
