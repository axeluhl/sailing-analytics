package com.sap.sse.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

public class SecurityServiceImpl implements SecurityService {
    
    private SecurityManager securityManager;
    
    public SecurityServiceImpl() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }

}
