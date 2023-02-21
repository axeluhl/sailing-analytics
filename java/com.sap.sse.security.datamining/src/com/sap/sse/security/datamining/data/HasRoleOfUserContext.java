package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasRoleOfUserContext extends HasRoleContext {
    @Connector(scanForStatistics = false)
    HasUserContext getUserContext();
}
