package com.sap.sse.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;

import com.sap.sse.security.userstore.shared.SocialUserAccount;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;

public interface SecurityService {

    SecurityManager getSecurityManager();

    Collection<User> getUserList();

    User getUserByName(String username);

    User getCurrentUser();

    /**
     * Returns the redirect URL
     */
    String login(String username, String password) throws UserManagementException;

    String getAuthenticationUrl(Credential credential) throws UserManagementException;

    User verifySocialUser(Credential credential) throws UserManagementException;

    void logout();

    User createSimpleUser(String username, String email, String password) throws UserManagementException;

    User createSocialUser(String username, SocialUserAccount socialUserAccount) throws UserManagementException;

    void deleteUser(String username) throws UserManagementException;

    Set<String> getRolesFromUser(String username) throws UserManagementException;

    void addRoleForUser(String username, String role) throws UserManagementException;

    void removeRoleFromUser(String username, String role) throws UserManagementException;

    void addSetting(String key, Class<?> clazz) throws UserManagementException;

    void setSetting(String key, Object setting);

    <T> T getSetting(String key, Class<T> clazz);

    Map<String, Object> getAllSettings();

    Map<String, Class<?>> getAllSettingTypes();

    void refreshSecurityConfig(ServletContext context);

    CacheManager getCacheManager();
}
