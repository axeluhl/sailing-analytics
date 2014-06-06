package com.sap.sse.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.userstore.shared.SocialUserAccount;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class SecurityServiceImpl  extends RemoteServiceServlet implements SecurityService {
    
    private static final long serialVersionUID = -3490163216601311858L;
    
    private SecurityManager securityManager;
    private UserStore store;
    
    public SecurityServiceImpl() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        System.out.println("Loaded shiro.ini file from: classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        Logger.getLogger(SecurityServiceImpl.class.getName()).info("Created: " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
        
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        store = (UserStore) context.
                getService(serviceReference);
        
        //Create default users if no users exist yet.
        if (store.getUserCollection().isEmpty()){
            try {
                createSimpleUser("Ben", "Ben@sapsailing.com", "ben123");
                addRoleForUser("Ben", "admin");
                addRoleForUser("Ben", "moderator");
                createSimpleUser("Peter", "Peter@sapsailing.com", "peter123");
                addRoleForUser("Peter", "moderator");
                createSimpleUser("Hans", "Hans@sapsailing.com", "hans123");
                createSimpleUser("Hubert", "Hubert@sapsailing.com", "hubert123");
                createSimpleUser("Franz", "Franz@sapsailing.com", "franz123");
            } catch (UserManagementException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }

    @Override
    public Collection<User> getUserList() {
        return store.getUserCollection();
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        String redirectUrl;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        System.out.println("Trying to login: " + username);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
        HttpServletRequest httpRequest = WebUtils.getHttpRequest(subject);
        SavedRequest savedRequest = WebUtils.getSavedRequest(httpRequest);
        if (savedRequest != null){
            System.out.println("Found saved request");
            redirectUrl = savedRequest.getRequestUrl();
        }
        else {
            redirectUrl = "";
        }
        System.out.println("Redirecturl: " + redirectUrl);
        return redirectUrl;
    }

    @Override
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
    }

    @Override
    public User getUserByName(String name) {
        return store.getUserByName(name);
    }

    @Override
    public User createSimpleUser(String name, String email, String password) throws UserManagementException {
        if (store.getUserByName(name) != null){
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        if (name == null || password == null || name.length() < 3 || password.length() < 5){
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        UsernamePasswordAccount upa = new UsernamePasswordAccount(name, hashedPasswordBase64, salt);
        return store.createUser(name, email, upa);
    }

    @Override
    public Set<String> getRolesFromUser(String name) throws UserManagementException {
        return store.getRolesFromUser(name);
    }

    @Override
    public void addRoleForUser(String name, String role) throws UserManagementException {
        store.addRoleForUser(name, role);
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        store.removeRoleFromUser(name, role);
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        store.deleteUser(username);
    }

    @Override
    public void setSettings(String key, Object setting) {
        store.setSetting(key, setting);
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {
        return store.getSetting(key, clazz);
    }

    @Override
    public Map<String, Object> getAllSettings() {
        return store.getAllSettings();
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {
        return store.getAllSettingTypes();
    }

    @Override
    public User createSocialUser(String name, SocialUserAccount socialUserAccount) throws UserManagementException {
        if (store.getUserByName(name) != null){
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        return store.createUser(name, socialUserAccount.getEmail(), socialUserAccount);
    }

}
