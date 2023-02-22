package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasRoleOfUserInUserGroupContext extends HasRoleContext {
    @Connector(scanForStatistics = false)
    HasUserInUserGroupContext getUserInUserGroupContext();
}
