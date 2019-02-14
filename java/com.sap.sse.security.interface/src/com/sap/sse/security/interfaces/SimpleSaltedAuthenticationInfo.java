package com.sap.sse.security.interfaces;

import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

public class SimpleSaltedAuthenticationInfo implements SaltedAuthenticationInfo {
    private static final long serialVersionUID = -8423637970683173260L;
    private String username;
    private String saltedPassword;
    private byte[] salt;

    public SimpleSaltedAuthenticationInfo(String username, String saltedPassword, byte[] salt) {
        this.username = username;
        this.saltedPassword = saltedPassword;
        this.salt = salt;
    }

    @Override
    public PrincipalCollection getPrincipals() {
        PrincipalCollection coll = new SimplePrincipalCollection(username, username);
        return coll;
    }

    @Override
    public Object getCredentials() {
        return saltedPassword;
    }

    @Override
    public ByteSource getCredentialsSalt() {
        return new SimpleByteSource(salt); 
    }

}
