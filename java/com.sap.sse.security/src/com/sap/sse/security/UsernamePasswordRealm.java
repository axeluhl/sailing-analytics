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

import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class UsernamePasswordRealm extends AuthorizingRealm {
    
    private final UserStore store;
    
    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a
     * default constructor makes it difficult to get access to the user store
     * implementation which may live in a bundle that this bundle has no direct
     * access to. Therefore, test cases must set the UserStore implementation
     * by invoking {@link #setTestUserStore} before the default constructor is
     * invoked.
     */
    private static UserStore testUserStore;
    
    public static void setTestUserStore(UserStore theTestUserStore) {
        testUserStore = theTestUserStore;
    }
    
    public UsernamePasswordRealm() {
        super();
        BundleContext context = Activator.getContext();
        if (context == null) {
            // non-OSGi case, such as during JUnit test execution?
            store = testUserStore;
        } else {
            ServiceReference<?> serviceReference = context.getServiceReference(UserStore.class.getName());
            store = (UserStore) context.getService(serviceReference);
        }
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
                roles.addAll(store.getRolesFromUser(username));
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
        ByteSource salt = null;
        User user = store.getUserByName(username);
        if (user == null){
            return null;
        }
        UsernamePasswordAccount upa = (UsernamePasswordAccount) user.getAccount(AccountType.USERNAME_PASSWORD);
        if (upa == null){
            return null;
        }
        saltedPassword = upa.getSaltedPassword();
        salt = (ByteSource) upa.getSalt();
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
