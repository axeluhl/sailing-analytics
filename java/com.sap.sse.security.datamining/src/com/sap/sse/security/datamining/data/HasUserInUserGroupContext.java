package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.security.shared.impl.User;

public interface HasUserInUserGroupContext {
    HasUserGroupContext getUserGroupContext();
    
    User getUser();

    @Dimension(messageKey="User")
    default String getName() {
        return getUser().getName();
    }
}
