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
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;

public class UsernamePasswordRealm extends AuthorizingRealm {
    
    private UserStore store;
    
    public UsernamePasswordRealm() {
        super();
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        store = (UserStore) context.
                getService(serviceReference);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo ai = new SimpleAuthorizationInfo();
        List<String> roles = new ArrayList<>();
        for (Object r : principals.asList()){
            String name = r.toString();
            try {
                roles.addAll(store.getRolesFromUser(name));
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
        try {
            saltedPassword = store.getSaltedPassword(username);
        } catch (UserManagementException e) {
            throw new AuthenticationException(e.getMessage());
        }
        if (saltedPassword == null) {
            return null;
        }

        ByteSource salt = null;
        try {
            salt = (ByteSource) store.getSalt(username);
        } catch (UserManagementException e) {
            throw new AuthenticationException(e.getMessage());
        }
        if (salt == null) {
            return null;
        }
        
        // return salted credentials
        SaltedAuthenticationInfo sai = new SimpleSaltedAuthenticationInfo(username, saltedPassword, salt);
        return sai;
    }


}
