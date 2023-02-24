package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;

public interface HasPreferenceOfUserInUserGroupContext extends HasPreferenceContext {
    @Connector(messageKey = "UsersInUserGroup", scanForStatistics = false)
    HasUserInUserGroupContext getUserInUserGroupContext();
}
