package com.sap.sse.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;

public class UsernamePasswordRealm extends AbstractUserStoreBasedRealm {
    
    public UsernamePasswordRealm() {
        super();
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
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        final SimpleAuthorizationInfo ai = new SimpleAuthorizationInfo();
        final List<String> roles = new ArrayList<>();
        for (Object r : principals.asList()){
            String username = r.toString();
            try {
                roles.addAll(getUserStore().getRolesFromUser(username));
            } catch (UserManagementException e) {
               throw new AuthenticationException(e.getMessage());
            }
        }
        ai.addRoles(roles);
        return ai;
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
        if (user == null){
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
