package com.sap.sse.security.datamining.data;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.UserGroup;

public interface HasUserGroupContext {
    @Connector(ordinal=0)
    UserGroup getUserGroup();
    
    @Dimension(messageKey="ImpliesRoles")
    default boolean isImpliesRoles() {
        return !getUserGroup().getRoleDefinitionMap().isEmpty();
    }
    
    @Statistic(messageKey="NumberOfUsersInUserGroup")
    default int getNumberOfUsersInUserGroup() {
        return Util.size(getUserGroup().getUsers());
    }

    SecurityService getSecurityService();
}
