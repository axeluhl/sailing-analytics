package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.UserGroup;

public interface HasUserGroupContext {
    @Connector(messageKey="UserGroup", ordinal=0)
    UserGroup getUserGroup();

    SecurityService getSecurityService();
}
