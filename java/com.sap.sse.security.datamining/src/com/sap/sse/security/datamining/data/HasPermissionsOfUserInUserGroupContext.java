package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasPermissionsOfUserInUserGroupContext {
    @Connector
    HasUserInUserGroupContext getUserInUserGroupContext();

    @Dimension(messageKey="PermissionString", ordinal=0)
    String getPermissionString();
    
    @Dimension(messageKey="PermissionTypes", ordinal=1)
    String getPermissionTypes();
    
    @Dimension(messageKey="PermissionActions", ordinal=2)
    String getPermissionActions();
    
    @Dimension(messageKey="PermissionObjects", ordinal=3)
    String getPermissionObjects();
}
