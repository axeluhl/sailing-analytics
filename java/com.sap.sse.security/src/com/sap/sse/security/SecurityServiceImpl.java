package com.sap.sse.security;

import org.apache.shiro.mgt.SecurityManager;

public class SecurityServiceImpl implements SecurityService {
    
    private SecurityManager securityManager;
    
    public SecurityServiceImpl(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }

}
