package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.impl.User;

public class UserWithContext implements HasUserContext {
    private final User user;
    private final SecurityService securityService;
    
    public UserWithContext(User user, SecurityService securityService) {
        super();
        this.user = user;
        this.securityService = securityService;
    }

    @Override
    public User getUser() {
        return user;
    }
    
    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }
}
