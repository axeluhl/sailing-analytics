package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasRoleOfUserContext extends HasRoleContext {
    @Connector(messageKey = "User", scanForStatistics = false)
    HasUserContext getUserContext();
}
