package com.sap.sse.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;

import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.interfaces.SimpleSaltedAuthenticationInfo;
import com.sap.sse.security.shared.UsernamePasswordAccount;

public class UsernamePasswordRealm extends AbstractCompositeAuthorizingRealm {
    
    public UsernamePasswordRealm() {
        super();
        setAuthenticationTokenClass(UsernamePasswordToken.class);
    }
    
    @Override
    public boolean supports(AuthenticationToken token) {
        final boolean result;
        if (token == null) {
            result = false;
        } else if (! (token instanceof UsernamePasswordToken)) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;
        final String username = userPassToken.getUsername();

        if (username == null) {
            return null;
        }

        // read password hash and salt from db
        String saltedPassword = null;
        byte[] salt = null;
        User user = getUserStore().getUserByName(username);
        if (user == null) {
            return null;
        }
        UsernamePasswordAccount upa = (UsernamePasswordAccount) user.getAccount(AccountType.USERNAME_PASSWORD);
        if (upa == null){
            return null;
        }
        saltedPassword = upa.getSaltedPassword();
        salt = upa.getSalt();
        if (saltedPassword == null) {
            return null;
        }
        if (salt == null) {
            return null;
        }
        
        // return salted credentials
        SaltedAuthenticationInfo sai = new SimpleSaltedAuthenticationInfo(username, saltedPassword, salt);
        return sai;
    }


}
