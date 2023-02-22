package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasUserInUserGroupContext extends HasUserContext {
    @Connector(scanForStatistics = false)
    HasUserGroupContext getUserGroupContext();
}
