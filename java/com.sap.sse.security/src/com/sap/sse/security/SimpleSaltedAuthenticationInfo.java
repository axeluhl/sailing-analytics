package com.sap.sse.security;

import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;

public class SimpleSaltedAuthenticationInfo implements SaltedAuthenticationInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -8423637970683173260L;
    private String username;
    private String password;
    private ByteSource salt;

    public SimpleSaltedAuthenticationInfo(String username, String password, ByteSource salt) {
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    @Override
    public PrincipalCollection getPrincipals() {
        PrincipalCollection coll = new SimplePrincipalCollection(username, username);
        return coll;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public ByteSource getCredentialsSalt() {
        return  salt; 
    }

}
